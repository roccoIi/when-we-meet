package com.whenwemeet.backend.domain.user.service;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;

public interface UserService {
    UserInfoResponse getUserInfo(Long userId);

    void changeNickname(Long userId, String nickname);
}
