package com.example.adminservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "API 응답의 기본 형식")
public class BaseResponse {

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "성공 응답")
public static class Success<T> {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success = true;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;
    
    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;
    
    @Schema(description = "응답 데이터")
    private final T data;

    private Success(String message, int status, T data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이지네이션이 포함된 성공 응답")
public static class PagedSuccess<T> {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success = true;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;
    
    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;
    
    @Schema(description = "응답 데이터")
    private final T data;
    
    @Schema(description = "현재 페이지 번호", example = "0")
    private final int currentPage;
    
    @Schema(description = "전체 항목 수", example = "45")
    private final long totalItems;
    
    @Schema(description = "전체 페이지 수", example = "5")
    private final int totalPages;

    private PagedSuccess(String message, int status, T data, int currentPage, long totalItems, int totalPages) {
        this.message = message;
        this.status = status;
        this.data = data;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "오류 응답")
public static class Error {
    @Schema(description = "성공 여부", example = "false")
    private final boolean success = false;
    
    @Schema(description = "오류 메시지", example = "요청을 처리하는 중 오류가 발생했습니다.")
    private final String message;
    
    @Schema(description = "오류 코드", example = "INTERNAL_ERROR")
    private final String errorCode;
    
    @Schema(description = "HTTP 상태 코드", example = "500")
    private final int status;

    private Error(String message, String errorCode, int status) {
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
}

/**
 * 성공 응답 생성 (200 OK)
 */
public static <T> Success<T> success(T data, String message) {
    return new Success<>(message, HttpStatus.OK.value(), data);
}

/**
 * 성공 응답 생성 (상태 코드 지정)
 */
public static <T> Success<T> success(T data, String message, int status) {
    return new Success<>(message, status, data);
}

/**
 * 오류 응답 생성 (500 서버 오류)
 */
public static Error fail(String message) {
    return new Error(message, "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value());
}

/**
 * 오류 응답 생성 (상태 코드 지정)
 */
public static Error fail(String message, int status) {
    return new Error(message, "INTERNAL_ERROR", status);
}

/**
 * 오류 응답 생성 (오류 코드와 상태 코드 지정)
 */
public static Error fail(String message, String errorCode, int status) {
    return new Error(message, errorCode, status);
}

/**
 * 페이지네이션이 포함된 성공 응답 생성 (200 OK)
 */
public static <T> PagedSuccess<T> successPaged(T data, String message, int currentPage, long totalItems, int totalPages) {
    return new PagedSuccess<>(message, HttpStatus.OK.value(), data, currentPage, totalItems, totalPages);
}

/**
 * 페이지네이션이 포함된 성공 응답 생성 (상태 코드 지정)
 */
public static <T> PagedSuccess<T> successPaged(T data, String message, int status, int currentPage, long totalItems, int totalPages) {
    return new PagedSuccess<>(message, status, data, currentPage, totalItems, totalPages);
}
}
