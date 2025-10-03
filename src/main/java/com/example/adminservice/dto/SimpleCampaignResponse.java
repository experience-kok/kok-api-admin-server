package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 간단한 캠페인 응답 DTO (목록 조회용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "간단한 캠페인 정보")
public class SimpleCampaignResponse {

    @Schema(description = "캠페인 ID", example = "125")
    private Long id;

    @Schema(description = "캠페인 제목", example = "운동화 리뷰 캠페인")
    private String title;

    @Schema(description = "캠페인 타입", example = "유튜브")
    private String campaignType;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/shoes.jpg")
    private String thumbnailUrl;

    @Schema(description = "제품 간단 정보", example = "신제품 운동화 체험 및 리뷰")
    private String productShortInfo;

    @Schema(description = "최대 신청자 수", example = "15")
    private Integer maxApplicants;

    @Schema(description = "모집 시작일", example = "2025-08-01")
    private LocalDate recruitmentStartDate;

    @Schema(description = "모집 종료일", example = "2025-08-15")
    private LocalDate recruitmentEndDate;

    @Schema(description = "선정일", example = "2025-08-16")
    private LocalDate selectionDate;

    @Schema(description = "승인 상태", example = "승인됨")
    private String approvalStatus;

    @Schema(description = "승인/거절 코멘트", example = "제품 정보가 상세하고 일정이 적절하여 승인합니다")
    private String approvalComment;

    @Schema(description = "승인 처리 일시", example = "2025-07-14T14:20:00")
    private LocalDateTime approvalDate;

    @Schema(description = "캠페인 카테고리 정보")
    private CategoryDTO category;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "카테고리 정보")
    public static class CategoryDTO {
        @Schema(description = "카테고리 타입", example = "방문")
        private String type;

        @Schema(description = "카테고리 이름", example = "기타")
        private String name;
    }
}
