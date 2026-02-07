package com.whenwemeet.backend.global.security.filter;

import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.whenwemeet.backend.global.exception.ErrorCode.C001;

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

    /**
     * 모든 API 요청에 대해서 AccessToken 검증을 진행합니다.
     * 1) 토큰이 존재하고 유효할경우 -> 현재 토큰을 기준으로 Authentication 객체를 만들어 저장합니다.
     * 2) 토큰이 존재하지만 만료됐을 경우 -> 토큰이 만료됐으니 401에러를 반환하고, 프론트에서 401에러를 인터셉트해서 재발급 API 를 호출합니다.
     * 3) 토큰이 없을경우 -> 비로그인 사용자로 가정하고, 발행된 임시토큰을 기준으로 사용자를 식별합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String token = getTokenFromRequest(request);

            // 1) AccessToken 토큰이 없는 경우
            if(token == null){
                log.info("AccessToken 없음");
                String refreshToken = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);

                // i ) refreshToken은 있는 경우
                if(refreshToken != null){
                    log.info("AccessToken은 없지만 RefreshToken은 있으므로 재발급 진행");
                    filterChain.doFilter(request, response);
                    return;
                }

                // ii) refreshToken도 없는 경우
                //     guestToken을 발급하고 Authentication 객체를 만들어 다음 필터로 넘겨야한다.
                else {
                    // 여기에 봇, 크롤러를 방지할 수 있는 공통점을 찾아야한다. 단순조회에서 User를 생성하면 DB과부화 위험
//                    if("GET".equals(request.getMethod())){
//                        filterChain.doFilter(request, response);
//                        return;
//                    }

                    token = jwtUtil.tokenByCookie(request, GUEST_TOKEN_NAME);

                    // guestToken도 없는 경우 -> User 객체 생성해서 토큰생성
                    User user;
                    if(token == null){
                        log.info("guestToken도 없으므로 User객체 생성");
                        user = User.createGuest();
                        userRepository.save(user);
                    }

                    // guestToken이 있는 경우 -> 기존토큰으로 userID 찾아서 새로운 토큰 생성
                    else {
                        log.info("보유한 guestToken으로 User식별 후 해당 유저 반환");
                       Long userId = jwtUtil.getUserId(token);
                       user = userRepository.findById(userId)
                               .orElseThrow(() -> new NotFoundException(C001));
                    }

                    log.info("guestToken 재발급 진행");
                    token = jwtUtil.generateGuestToken(user.getId(), response);
                }
            } else {
                // 2) 토큰은 있지만 만료된경우
                if(!jwtUtil.validateToken(token)){
                    log.info("AccessToken은 존재하지만 만료됐으므로 재발급 필요");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다");
                    return;
                }
            }

            log.info("토큰 정상확인. Authentication 객체 생성");
            // 3) 토큰도 있고 유효할경우: SecurityContextHolder 에 사용자정보 저장
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
