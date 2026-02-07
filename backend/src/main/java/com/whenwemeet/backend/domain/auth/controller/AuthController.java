package com.whenwemeet.backend.domain.auth.controller;

import com.whenwemeet.backend.domain.auth.service.AuthService;
import com.whenwemeet.backend.global.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<?>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response){
        log.info("reissue access token");
        authService.reissueToken(request, response);
        return ResponseEntity.ok(CommonResponse.success());
    }

}
