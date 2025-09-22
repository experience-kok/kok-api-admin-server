package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 활동 아이템 DTO (USER의 신청내역 또는 CLIENT의 캠페인)
 */
@Data
@Builder
@Schema(description = "활동 아이템 (사용자의 신청내역 또는 클라이언트의 캠페인)")
public class ActivityItemDto {
    
    @Schema(description = "ID (USER: 신청 ID, CLIENT: 캠페인 ID)", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    
    @Schema(description = "캠페인 제목", example = "카페 방문 체험 캠페인", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "회사명", example = "스타벅스 코리아", nullable = true)
    private String company;
    
    @Schema(description = "캠페인 타입", example = "블로그", allowableValues = {"인스타그램", "블로그","유튜브"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String campaignType;

    @Schema(description = "상태 (한글)", example = "완료", requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusText;
    
    @Schema(description = "생성일/신청일", example = "2025-07-10T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일", example = "2025-07-15T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updatedAt;
    
    // USER 타입일 때만 사용 (캠페인 신청 내역)
    @Schema(description = "캠페인 ID (USER 타입에서만 사용)", example = "20", nullable = true)
    private Long campaignId;
    
    @Schema(description = "최대 신청자 수 (USER 타입에서만 사용)", example = "50", nullable = true)
    private Integer maxApplicants;
    
    // CLIENT 타입일 때만 사용 (생성한 캠페인)
    @Schema(description = "현재 신청자 수 (CLIENT 타입에서만 사용)", example = "23", nullable = true)
    private Integer currentApplications;
    
    @Schema(description = "승인자명 (CLIENT 타입에서만 사용)", example = "관리자", nullable = true)
    private String approvedBy;
    
    @Schema(description = "승인일 (CLIENT 타입에서만 사용)", example = "2025-07-09T15:30:00", nullable = true)
    private LocalDateTime approvalDate;
    
    @Schema(description = "모집 기간 (CLIENT 타입에서만 사용)", example = "2025-07-10 ~ 2025-07-20", nullable = true)
    private String recruitmentPeriod;
}
