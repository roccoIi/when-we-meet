package com.whenwemeet.backend.domain.user.repository.custom;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;

import java.util.Optional;

public interface UserCustomRepository {

    Optional<UserInfoResponse> findInfoByUserId(Long userId);
}
