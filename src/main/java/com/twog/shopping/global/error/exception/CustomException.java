package com.twog.shopping.global.error.exception;

import com.twog.shopping.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
}
