package com.example.adminservice.constant;

import lombok.Getter;

@Getter
public enum AdminErrorCode {
    UNAUTHORIZED("AUTH001", "인증에 실패했습니다."),
    ACCESS_DENIED("AUTH002", "접근 권한이 없습니다."),
    INVALID_TOKEN("AUTH003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH004", "만료된 토큰입니다."),

    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다."),
    ADMIN_NOT_FOUND("USER002", "관리자 권한이 없습니다."),

    SERVER_ERROR("SYS001", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;

    AdminErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}