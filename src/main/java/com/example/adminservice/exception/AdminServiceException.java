package com.example.adminservice.exception;

import com.example.adminservice.constant.AdminErrorCode;
import lombok.Getter;

@Getter
public class AdminServiceException extends RuntimeException {
    private final AdminErrorCode errorCode;

    public AdminServiceException(String message, AdminErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AdminServiceException(AdminErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}