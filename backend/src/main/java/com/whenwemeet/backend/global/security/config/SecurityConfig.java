package com.whenwemeet.backend.global.security.config;

import com.whenwemeet.backend.global.security.filter.JwtAuthenticationFilter;
import com.whenwemeet.backend.global.security.handler.OAuth2FailureHandler;
import com.whenwemeet.backend.global.security.handler.OAuth2SuccessHandler;
import com.whenwemeet.backend.global.security.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  {

        http
                .cors((corsCustomizer) -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();

                    configuration.setAllowedOrigins(List.of(
                            "https://whenwemeet.site",
                            "https://www.whenwemeet.site",
                            "http://localhost:5173"
                    ));
                    configuration.setAllowedMethods(List.of("*"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setExposedHeaders(List.of("Authorization"));
                    configuration.setAllowCredentials(true);
                    configuration.setMaxAge(3600L);

                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", configuration);
                    return configuration;
                }))


                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rq -> rq
                        .requestMatchers(
                                "/api/auth/**", // oauth 토큰 재발급
                                "/oauth2/**",   // oauth2 리다이렉트
                                "/login/oauth2/**",  // oauth2 리다이렉트
                                "/api/meetings",  // meetingList 반환(home)
                                "/api/meetings/create", // meeting 생성
                                "/api/meetings/share",  // 초대링크 접속시 요약정보 반환
                                "/api/user/first"  // 첫 접속 계정생성
                        ).permitAll()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(login -> login
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .formLogin(login -> login.disable());


        return http.build();
    }
}
