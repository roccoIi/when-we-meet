package com.whenwemeet.backend.domain.user.dto.response;

public record UserInfoResponse(
        String nickname,
        String provider,
        String profileImgUrl
) {}
