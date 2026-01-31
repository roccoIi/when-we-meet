package com.whenwemeet.backend.global.exception;

import com.whenwemeet.backend.global.exception.type.DuplicateException;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.exception.type.UnAuthorizedException;
import com.whenwemeet.backend.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<CommonResponse> handleNotFoundException(final NotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.of(
                        errorCode.getMessage(),
                        errorCode.name()
                ));
    }

    @ExceptionHandler(DuplicateException.class)
    public final ResponseEntity<CommonResponse> handleDuplicateException(final DuplicateException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.of(
                        errorCode.getMessage(),
                        errorCode.name()
                ));
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public final ResponseEntity<CommonResponse> handleUnAuthorizedException(final UnAuthorizedException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.of(
                        errorCode.getMessage(),
                        errorCode.name()
                ));
    }

}
