package com.whenwemeet.backend.global.response;

import com.whenwemeet.backend.global.entity.Pagination;
import com.whenwemeet.backend.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonResponse<T> {
    private String code;
    private String message;
    private T data;
    private Pagination pagination;

    public CommonResponse(String message, String code, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public CommonResponse(String message, String code, T data, Pagination pagination) {
        this.message = message;
        this.code = code;
        this.data = data;
        this.pagination = pagination;
    }

    public static <T> CommonResponse<T> of(String message, String code) {
        return new CommonResponse<>(message, code, null);
    }

    public static <T> CommonResponse<T> of(ErrorCode responseCode, T data) {
        return new CommonResponse<>(responseCode.getMessage(), responseCode.name(), data);
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("SUCCESS", "200", data);
    }

    public static <T> CommonResponse<T> success(T data, Pagination pagination) {
        return new CommonResponse<>("SUCCESS", "200", data, pagination);
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>("SUCCESS", "200", null);

    }
}

