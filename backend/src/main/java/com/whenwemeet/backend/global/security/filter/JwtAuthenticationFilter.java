package com.whenwemeet.backend.global.security.filter;

import com.whenwemeet.backend.global.security.authentication.AuthenticationFactory;
import com.whenwemeet.backend.global.util.JwtUtil;
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

    @Value("${spring.jwt.name.refresh-token}")
    private String REFRESH_TOKEN_NAME;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // JWT 필터를 건너뛸 URL 목록
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/login"
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
            String token = getTokenFromRequest(request);

            // --- 1) AccessToken이 없는 경우
            // |     -> RefreshToken이 있는지 Cookie에서 확인
            // |      ㄴ I)  RefreshToken이 있는 경우
            // |             ㄴ RefreshToken을 사용해 user를 가져오고, 해당 User로 AccessToken생성, RefreshToken 재발급
            // |      ㄴ II) RefreshToken이 없는 경우
            // |             ㄴ 아무런 존재가 아닌 게스트사용자 이므로 다음 필터로 넘깁니다.
            // --- 2) AccessToken이 있는 경우
            //        ㄴ I) 해당 토큰을 활용해서 Authentication 생성


            if(token == null || !jwtUtil.validateToken(token)) {
                // 1) AccessToken이 없는 경우 RefreshToken이 있는지 Cookie에서 확인g
                token = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);

                if(token != null && jwtUtil.verifyRefreshToken(token)) {
                    // I)  RefreshToken이 있는 경우

                    // I-1) accessToken 발급 후 헤더에 저장
                    Long userId = jwtUtil.getUserId(token);
                    token = jwtUtil.generateAccessToken(userId);
                    response.setHeader("Authorization", "Bearer " + token);

                    // I-2) refreshToken 재발급 후 쿠키 갱신 및 redis 갱신
                    jwtUtil.generateRefreshToken(userId, response);
                }
                else {
                    // II) RefreshToken이 없는 경우 -> 아무런 존재가 아닌 게스트사용자 이므로 다음 필터로 넘깁니다.
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // 2) AccessToken이 있는 경우 + refreshToken으로 재발급후  해당 토큰을 활용해서 Authentication 생성
            Authentication authentication = authenticationFactory.createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e){
            log.error("인증 처리 중 오류발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        return token.substring(7);
    }
}
