package com.whenwemeet.backend.global.exception.type;

import com.whenwemeet.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UnAuthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnAuthorizedException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
