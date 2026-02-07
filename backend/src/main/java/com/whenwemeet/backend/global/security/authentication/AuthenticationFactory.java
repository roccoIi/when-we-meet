package com.whenwemeet.backend.global.security.authentication;

import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static com.whenwemeet.backend.global.exception.ErrorCode.U001;

@Component
@RequiredArgsConstructor
public class AuthenticationFactory {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public Authentication createAuthentication(String token) {
        // 1. AccessToken 에서 UserId 추출
        Long userId = jwtUtil.getUserId(token);

        // 2. 해당 UserId를 통해 User 객체 반환
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 3. User 객체를 활용해 Authentication 객체를 위한 CustomOAuth2User 객체 (principal)를 만든다.
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user);

        // 4. 최종적으로 Authentication 객체를 완성해서 SecurityContextHolder 에 넣는다.
        return new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities());
    }
}
