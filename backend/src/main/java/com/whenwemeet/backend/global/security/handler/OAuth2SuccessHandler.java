package com.whenwemeet.backend.global.security.handler;

import com.whenwemeet.backend.global.util.JwtUtil;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    @Value("${spring.login.target-uri}")
    private String targetUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // SecurityContextHolder에 User 인증정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println(authentication);

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 1. AccessToken 생성
        String accessToken = jwtUtil.generateAccessToken(customOAuth2User.getId());
        System.out.println("accessToken: " + accessToken);

        // 2. RefreshToken 생성 및 쿠키전달
        jwtUtil.generateRefreshToken(customOAuth2User.getId(), response);

        // 4. Redirect 진행
        response.sendRedirect(targetUrl + "?isLogin=true");
    }

}
