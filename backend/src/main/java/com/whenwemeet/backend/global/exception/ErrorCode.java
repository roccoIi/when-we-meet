package com.whenwemeet.backend.global.exception;

public enum ErrorCode {
    // TEST 관련 예외 코드
    T000("테스트 오류 입니다."),

    // USER 관련 예외 코드
    U000("OAuth2 로그인 진행시 소셜로그인 기록이 없는 사용자입니다."),
    U001("해당 PK를 가진 유저가 존재하지 않습니다."),

    // AUTH 관련 예외 코드
    A001("인증/인가시, 만료된 JWT"),
    A002("허가되지 않은 OAuth 로그인 도메인입니다."),

    // TOKEN 관련 예외 코드
    T001("RefreshToken이 존재하지 않습니다."),
    T002("RefreshToken이 유효하지 않습니다."),


    // Cookie 관련 예외코드
    C001("쿠키가 존재하지 않습니다.")
    ;

    private final String message;

    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
