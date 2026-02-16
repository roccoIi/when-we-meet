package com.whenwemeet.backend.domain.user.controller;

import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.dto.request.NickNameRequest;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.entity.UserType;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.domain.user.service.UserService;
import static com.whenwemeet.backend.global.exception.ErrorCode.*;

import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.response.CommonResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<CommonResponse<?>> getUserInfo(
            @AuthenticationPrincipal CustomOAuth2User user) {
        if(user == null) return ResponseEntity.ok(CommonResponse.success());

        UserInfoResponse response = userService.getUserInfo(user.getId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/nickname")
    public ResponseEntity<CommonResponse<?>> updateNickname(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody NickNameRequest request){
        userService.changeNickname(user.getId(), request.nickname());
        return ResponseEntity.ok(CommonResponse.success());
    }

    @PostMapping("/first")
    public ResponseEntity<CommonResponse<?>> addFirstUser(
            @RequestBody NickNameRequest request,
            HttpServletResponse response
    ){
        userService.makeFirstUser(request.nickname(), response);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/test")
    public ResponseEntity<CommonResponse<?>> testAPI (@AuthenticationPrincipal CustomOAuth2User user){
        UserType role = user.getUser().getRole();
        User detail = userRepository.findUserById(user.getId())
                .orElseThrow(() -> new NotFoundException(A001));
        if(role == UserType.GUEST){

            log.info("비로그인 사용자 식별입니다.");
            log.info(detail.toString());
            return ResponseEntity.ok(CommonResponse.success());
        }
        log.info("로그인 사용자 식별입니다.");
        log.info(detail.toString());
        return ResponseEntity.ok(CommonResponse.success());
    }


}
