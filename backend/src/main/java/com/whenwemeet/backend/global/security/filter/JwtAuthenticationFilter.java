package com.whenwemeet.backend.global.security.filter;

import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.util.JwtUtil;
import com.whenwemeet.backend.global.security.authentication.AuthenticationFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationFactory authenticationFactory;
    private final UserRepository userRepository;

    @Value("${spring.jwt.name.refresh-token}")
    private String REFRESH_TOKEN_NAME;

    @Value("${spring.jwt.name.guest-token}")
    private String GUEST_TOKEN_NAME;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // JWT 필터를 건너뛸 URL 목록
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/login"
    );

    // Guest 유저 생성 없이 접근 가능한 읽기 전용 URL 목록 (봇/크롤러 대응)
    private static final List<String> READ_ONLY_URLS = Arrays.asList(
            "/api/meetings",                   // GET: 미팅룸 리스트 조회
            "/api/meetings/share/**"          // GET: 공유링크를 통한 미팅룸 정보 조회
    );

    /**
     * 특정 경로에서 JWT 필터를 건너뛰도록 설정
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDE_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 읽기 전용 경로인지 확인 (Guest 유저 생성이 필요 없는 경로)
     */
    private boolean isReadOnlyPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // GET 요청이면서 READ_ONLY_URLS에 매칭되는 경로인 경우
        if ("GET".equals(method)) {
            return READ_ONLY_URLS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));
        }
        return false;
    }

    /**
     * 모든 API 요청에 대해서 AccessToken 검증을 진행합니다.
     * 1) 토큰이 존재하고 유효할경우 -> 현재 토큰을 기준으로 Authentication 객체를 만들어 저장합니다.
     * 2) 토큰이 존재하지만 만료됐을 경우 -> 토큰이 만료됐으니 401에러를 반환하고, 프론트에서 401에러를 인터셉트해서 재발급 API 를 호출합니다.
     * 3) 토큰이 없을경우:
     *    - 읽기 전용 경로: Guest 유저 생성 없이 통과 (봇/크롤러 대응)
     *    - 일반 경로: 비로그인 사용자로 가정하고, 발행된 임시토큰을 기준으로 사용자를 식별합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            log.info("헤더의 AccessToken 확인");
            String token = getTokenFromRequest(request);

            // --- 1) AccessToken이 없는 경우
            // |   2) RefreshToken이 있는지 Cookie에서 확인
            // |      ㄴ i)  RefreshToken이 있는 경우
            // |             ㄴ RefreshToken을 사용해 user를 가져오고, 해당 User로 AccessToken생성, RefreshToken 재발급
            // |      ㄴ ii) RefreshToken이 없는 경우
            // |             ㄴ 아무런 존재가 아닌 게스트사용자 이므로 다음 필터로 넘깁니다.
            // --- 1) AccessToken이 있는 경우
            //        ㄴ i) 해당 토큰을 활용해서 Authentication 생성


            if(token == null || !jwtUtil.validateToken(token)) {
                if(token == null) log.info("AccessToken이 없습니다");
                else if(!jwtUtil.validateToken(token)) log.info("AccessToken이 만료되었습니다.");

                log.info("AccessToken에 이상이 있으므로 RefreshToken을 확인합니다.");
                token = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);

                if(token != null && jwtUtil.verifyRefreshToken(token)) {
                    log.info("RefreshToken이 존재합니다. AccessToken 및 RefreshToken을 재발급합니다.");

                    // 1) accessToken 발급 후 헤더에 저장
                    Long userId = jwtUtil.getUserId(token);
                    token = jwtUtil.generateAccessToken(userId);
                    response.setHeader("Authorization", "Bearer " + token);

                    // 2) refreshToken 재발급 후 쿠키 갱신 및 redis 갱신
                    jwtUtil.generateRefreshToken(userId, response);
                }
                else {
                    log.info("RefreshToken이 존재하지 않습니다. 게스트로 인식하고 filter를 종료합니다.");
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            log.info("AccessToken이 존재합니다. Authentication 객체 생성 후 필터를 넘깁니다.");

            Authentication authentication = authenticationFactory.createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e){
            log.error("인증 처리 중 오류발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.info("🔍 Authorization 헤더 값: {}", token); // 추가

        if (token == null || !token.startsWith("Bearer ")) {
            if(token == null) log.info("Authorization 헤더가 없음");
            else if(!token.startsWith("Bearer ")) log.info("헤더가 Bearer로 시작하지 않음");
            return null;
        }
        return token.substring(7);
    }
}
