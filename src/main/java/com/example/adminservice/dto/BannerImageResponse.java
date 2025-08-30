package com.example.adminservice.dto;

import com.example.adminservice.domain.BannerImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배너 이미지 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배너 이미지 정보")
public class BannerImageResponse {

    @Schema(description = "배너 ID", example = "1")
    private Long id;

    @Schema(description = "배너 제목", example = "신규 캠페인 프로모션")
    private String title;

    @Schema(description = "배너 설명", example = "새로운 캠페인을 확인해보세요!")
    private String description;

    @Schema(description = "배너 이미지 URL", example = "https://ckokservice.s3.ap-northeast-2.amazonaws.com/banners/banner1.jpg")
    private String bannerUrl;

    @Schema(description = "클릭 시 이동할 URL", example = "https://example.com/campaign")
    private String redirectUrl;

    @Schema(description = "배너 포지션", example = "TOP")
    private String position;

    @Schema(description = "배너 표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "생성 시간", example = "2025-07-14T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-07-14T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * BannerImage 엔티티로부터 BannerImageResponse를 생성합니다.
     */
    public static BannerImageResponse from(BannerImage bannerImage) {
        return BannerImageResponse.builder()
                .id(bannerImage.getId())
                .title(bannerImage.getTitle())
                .description(bannerImage.getDescription())
                .bannerUrl(bannerImage.getBannerUrl())
                .redirectUrl(bannerImage.getRedirectUrl())
                .position(bannerImage.getPosition().name())
                .displayOrder(bannerImage.getDisplayOrder())
                .createdAt(bannerImage.getCreatedAt())
                .updatedAt(bannerImage.getUpdatedAt())
                .build();
    }
}
