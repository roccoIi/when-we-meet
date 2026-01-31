package com.whenwemeet.backend.global.security.filter;

import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
import com.whenwemeet.backend.global.security.authentication.AuthenticationFactory;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationFactory authenticationFactory;

    /**
     * 모든 API 요청에 대해서 AccessToken 검증을 진행합니다.
     * 1) 토큰이 존재하고 유효할경우 -> 현재 토큰을 기준으로 Authentication 객체를 만들어 저장합니다.
     * 2) 토큰이 존재하지만 만료됐을 경우 -> 토큰이 만료됐으니 401에러를 반환하고, 프론트에서 401에러를 인터셉트해서 재발급 API 를 호출합니다.
     * 3) 토큰이 없을경우 -> 비로그인 사용자로 가정하고, 발행된 임시토큰을 기준으로 사용자를 식별합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String accessToken = getTokenFromRequest(request);

            // 1) 토큰이 없는 경우: 비로그인
            if(accessToken == null){
                filterChain.doFilter(request, response);
                return;
            }

            // 2) 토큰은 있지만 만료된경우
            if(!jwtUtil.validateToken(accessToken)){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다");
                return;
            }

            // 3) 토큰도 있고 유효할경우: SecurityContextHolder 에 사용자정보 저장
            Authentication authentication = authenticationFactory.createAuthentication(accessToken);
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
