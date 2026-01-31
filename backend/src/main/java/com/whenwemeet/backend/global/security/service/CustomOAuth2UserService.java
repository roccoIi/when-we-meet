package com.whenwemeet.backend.global.security.service;

import com.whenwemeet.backend.global.security.dto.OAuth2Response;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser (OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 로그인한 유저 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Oauth 로그인 서비스 제공한 기업명 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 유저정보에 대한 DTO를 생성한다.
        OAuth2Response oAuth2Response = OAuth2Response.of(registrationId, oAuth2User.getAttributes());

        // 4. 회원가입 및 로그인
        User user = getUser(oAuth2Response);

        // 5. Security context에 저장할 객체 생성
        return new CustomOAuth2User(user);
//      System.out.println(oAuth2User.getAttributes());
//      System.out.println(userRequest.getClientRegistration());
    }

    /**
     * 서비스기업에서 제공한 유저정보의 provider, provider_id 기반으로 조회를 진행합니다.
     * 1) 만일 DB에 존재하는 사용자라면 해당 사용자의 정보 반환
     * 2) 만일 DB에 존재하지 않는다면 회원가입(DB Insert) 진행 후 반환
     * @param oAuth2Response OAuth 업체 측에서 제공한 사용자 정보
     * @return 현재 사용자 객체
     */
    private User getUser(OAuth2Response oAuth2Response){
        return userRepository.findUserByProviderAndProviderID(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                .orElseGet(() -> {
                    User newUser = oAuth2Response.toEntity();
                    return userRepository.save(newUser);
                });
    }
}
