package com.whenwemeet.backend.domain.user.service;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.entity.UserType;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import static com.whenwemeet.backend.global.exception.ErrorCode.*;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
import com.whenwemeet.backend.global.util.RandomProfile;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        return userRepository.findInfoByUserId(userId)
                .orElseThrow(() -> new NotFoundException(U001));
    }

    @Override
    @Transactional
    public void changeNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        log.info("닉네임 변경 진행");
        user.changeNickName(nickname);
        log.info("닉네임 변경 완료");
    }

    @Override
    @Transactional
    public void makeFirstUser(String nickname, HttpServletResponse response) {
        // 0) 프로필 생성
        String profile = new RandomProfile().generateProfileImg();

        // 1) 유저 생성
        User user = User.builder()
                .nickname(nickname)
                .role(UserType.GUEST)
                .profileImgUrl(profile)
                .build();

        // 2) 유저 저장
        userRepository.save(user);

        // 3) 토큰발급
        jwtUtil.generateAccessToken(user.getId(), response);
        jwtUtil.generateRefreshToken(user.getId(), response);
    }
}
