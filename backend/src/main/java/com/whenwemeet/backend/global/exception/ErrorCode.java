package com.whenwemeet.backend.global.exception;

public enum ErrorCode {
    // TEST 관련 예외 코드
    T000("테스트 오류 입니다."),

    // USER 관련 예외 코드
    U000("OAuth2 로그인 진행시 소셜로그인 기록이 없는 사용자입니다."),
    U001("등록되지 않은 사용자 입니다."),

    // AUTH 관련 예외 코드
    A001("인증/인가시, 만료된 JWT"),
    A002("허가되지 않은 OAuth 로그인 도메인입니다."),

    // TOKEN 관련 예외 코드
    T001("RefreshToken이 존재하지 않습니다."),
    T002("RefreshToken이 유효하지 않습니다."),
    T003("중복되지 않는 공유링크를 생성하는데 실패했습니다. (최대 횟수 10회)"),

    // Meeting 관련 예외 코드
    M001("해당 모임의 호스트가 아닙니다."),
    M002("사용자가 참여하지 않은 모임입니다."),
    M003("더이상 존재하지 않는 모임입니다."),
    M004("이미 참여중인 모임입니다."),
    M005("이미 만료된 초대링크입니다. 새로 발급된 초대링크를 확인해주세요"),

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
