package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * 캠페인 신청자 정보 DTO
 */
@Data
@Builder
@Schema(description = "캠페인 신청자 정보")
public class CampaignApplicantDto {
    
    @Schema(description = "신청자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    
    @Schema(description = "신청자 닉네임", example = "커피러버", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;
    
    @Schema(description = "신청자 이메일", example = "coffee@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "캠페인 신청일", example = "2025-07-20T14:30:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private ZonedDateTime appliedAt;
    
    @Schema(description = "신청 상태 (영문)", example = "SELECTED", allowableValues = {"APPLIED", "PENDING", "SELECTED", "REJECTED", "COMPLETED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String applicationStatus;
    
    @Schema(description = "신청 상태 (한글)", example = "선정", requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusText;
}
