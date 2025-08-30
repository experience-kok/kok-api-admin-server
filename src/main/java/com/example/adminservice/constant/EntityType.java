package com.example.adminservice.constant;

/**
 * 알림과 관련된 엔티티 타입을 정의하는 열거형
 */
public enum EntityType {
    CAMPAIGN("캠페인"),
    APPLICATION("신청"),
    USER("사용자"),
    COMPANY("회사"),
    BANNER("배너"),
    SYSTEM("시스템");

    private final String description;

    EntityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
