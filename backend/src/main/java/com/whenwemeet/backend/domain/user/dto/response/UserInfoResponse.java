package com.whenwemeet.backend.domain.user.dto.response;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
public class UserInfoResponse {
    private String nickname;
    private String profileImgUrl;
}
