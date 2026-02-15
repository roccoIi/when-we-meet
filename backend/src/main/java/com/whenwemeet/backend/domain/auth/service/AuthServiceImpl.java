package com.whenwemeet.backend.domain.auth.service;

import static com.whenwemeet.backend.global.exception.ErrorCode.*;

import com.whenwemeet.backend.global.exception.type.UnAuthorizedException;
import com.whenwemeet.backend.global.util.JwtUtil;
import com.whenwemeet.backend.global.security.authentication.AuthenticationFactory;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationFactory authenticationFactory;

    @Value("${spring.jwt.name.refresh-token}")
    private String REFRESH_TOKEN_NAME;

    @Override
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 0. 쿠키가 있는지 먼저 확인해야 한다.
        String refreshToken = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);
        if(refreshToken == null) throw new UnAuthorizedException(T001);

        // 2. 토큰이 있으면??? 그 토큰이 아직 유효한지 확인한다.
        if(!jwtUtil.validateToken(refreshToken) || !jwtUtil.verifyRefreshToken(refreshToken)) throw new UnAuthorizedException(T002);

        // 3. 모두 유효하다면 해당 토큰의 정보를 기반으로 새로운 Authentication 객체를 생성한다.
        Authentication authentication = authenticationFactory.createAuthentication(refreshToken);

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 4. 이후 refreshToken 재발급 후 쿠키로 넘긴다.
        jwtUtil.generateRefreshToken(customOAuth2User.getId(), response);

        // 5. accessToken도 재발급해서 헤더로 넘긴다.
        String accessToken = jwtUtil.generateAccessToken(customOAuth2User.getId());
        response.setHeader("Authorization", "Bearer " + accessToken);
    }
}
