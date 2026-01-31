package com.whenwemeet.backend.global.exception.type;

import com.whenwemeet.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException{
    private ErrorCode errorCode;

    public DuplicateException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
