package com.example.adminservice.constant;

/**
 * 캠페인 신청 상태
 */
public enum ApplicationStatus {
    APPLIED("신청"),
    PENDING("선정 대기중"),
    SELECTED("선정"),
    REJECTED("거절"),
    COMPLETED("완료");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
