package com.whenwemeet.backend.domain.user.service;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import static com.whenwemeet.backend.global.exception.ErrorCode.*;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;


    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        return userRepository.findInfoByUserId(userId)
                .orElseThrow(() -> new NotFoundException(A001));
    }

    @Override
    @Transactional
    public void changeNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(A001));

        log.info("닉네임 변경 진행");
        user.changeNickName(nickname);
        log.info("닉네임 변경 완료");
    }
}
