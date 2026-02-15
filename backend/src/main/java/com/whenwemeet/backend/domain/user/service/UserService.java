package com.whenwemeet.backend.domain.user.service;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserInfoResponse getUserInfo(Long userId);

    void changeNickname(Long userId, String nickname);

    void makeFirstUser(String nickname, HttpServletResponse response);
}
