package com.twog.shopping.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
