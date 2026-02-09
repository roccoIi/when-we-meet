package com.whenwemeet.backend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public record UserInfoResponse(
        String nickname,
        String provider,
        String profileImgUrl
) {}
