package com.example.adminservice.constant;

/**
 * 캠페인 신청 상태
 */
public enum ApplicationStatus {
    APPLIED("신청"),
    SELECTED("선정"),
    REJECTED("거절"),
    CANCELLED("취소");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
