package com.example.adminservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

@Schema(description = "API 응답의 기본 형식")
public class BaseResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "페이지네이션 정보")
    public static class Pagination {
        @Schema(description = "현재 페이지 번호 (1-based)", example = "1")
        private final int pageNumber;
        
        @Schema(description = "페이지당 항목 수", example = "10")
        private final int pageSize;
        
        @Schema(description = "전체 페이지 수", example = "5")
        private final int totalPages;
        
        @Schema(description = "전체 항목 수", example = "45")
        private final long totalElements;
        
        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private final boolean first;
        
        @Schema(description = "마지막 페이지 여부", example = "false")
        private final boolean last;

        public Pagination(int pageNumber, int pageSize, int totalPages, long totalElements, boolean first, boolean last) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.first = first;
            this.last = last;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public boolean isFirst() {
            return first;
        }

        public boolean isLast() {
            return last;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "페이지네이션이 포함된 데이터")
    public static class PagedData<T> {
        @Schema(description = "실제 데이터")
        private final T content;
        
        @Schema(description = "페이지네이션 정보")
        private final Pagination pagination;

        public PagedData(T content, Pagination pagination) {
            this.content = content;
            this.pagination = pagination;
        }

        public T getContent() {
            return content;
        }

        public Pagination getPagination() {
            return pagination;
        }
    }

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
     * 페이지네이션이 포함된 성공 응답 생성 (Spring Data Page 객체 사용)
     */
    public static <T> Success<PagedData<T>> successPaged(T content, String message, Page<?> page) {
        Pagination pagination = new Pagination(
            page.getNumber() + 1, // Spring Data는 0-based, 응답은 1-based
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast()
        );
        
        PagedData<T> pagedData = new PagedData<>(content, pagination);
        return new Success<>(message, HttpStatus.OK.value(), pagedData);
    }

    /**
     * 페이지네이션이 포함된 성공 응답 생성 (직접 파라미터 지정)
     */
    public static <T> Success<PagedData<T>> successPaged(T content, String message, 
                                                        int pageNumber, int pageSize, 
                                                        int totalPages, long totalElements) {
        boolean first = pageNumber == 1;
        boolean last = pageNumber >= totalPages;
        
        Pagination pagination = new Pagination(pageNumber, pageSize, totalPages, totalElements, first, last);
        PagedData<T> pagedData = new PagedData<>(content, pagination);
        
        return new Success<>(message, HttpStatus.OK.value(), pagedData);
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
}
