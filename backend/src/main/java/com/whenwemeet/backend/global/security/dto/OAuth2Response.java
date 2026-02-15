package com.whenwemeet.backend.global.security.dto;

import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.entity.UserType;
import com.whenwemeet.backend.global.exception.type.UnAuthorizedException;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

import static com.whenwemeet.backend.global.exception.ErrorCode.A002;

@Builder
@Getter
public class OAuth2Response {

    // 정보 제공자(Kakao, Google, Naver, ...)
    String provider;

    // 제공자 측 ID
    String providerId;

    // 사용자 설정 닉네임
    String nickname;

    // 사용자 설정 프로필사진 섬네일
    String thumbnail;

    // registrationId에 따라 OAuth2UserInfo 객체를 생성하는 정적 팩토리 메서드
    public static OAuth2Response of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> ofKakao(attributes);
            case "google" -> ofGoogle(attributes);
            default -> throw new UnAuthorizedException(A002);
        };
    }

    // Kakao 사용자 정보 매핑 메서드
    private static OAuth2Response ofKakao(Map<String, Object> attributes) {
        final Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");;

        return OAuth2Response.builder()
                .provider("kakao")
                .providerId(attributes.get("id").toString())
                .nickname(properties.get("nickname").toString())
                .thumbnail(convertHttps(properties.get("thumbnail_image").toString()))
                .build();
    }

    // google 사용자 정보 매핑 메서드
    private static OAuth2Response ofGoogle(Map<String, Object> attributes) {
        return OAuth2Response.builder()
                .provider("google")
                .providerId(attributes.get("id").toString())
                .nickname(attributes.get("name").toString())
                .thumbnail(convertHttps(attributes.get("picture").toString()))
                .build();

    }

    public static String convertHttps(String thumbnail) {
        if (thumbnail.startsWith("http://")) {
            return thumbnail.replace("http://", "https://");
        }
        return thumbnail;
    }

    public User toEntity(){
        return User.builder()
                .provider(provider)
                .providerID(providerId)
                .role(UserType.MEMBER)
                .nickname(nickname)
                .profileImgUrl(thumbnail)
                .build();
    }
}
