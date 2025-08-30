package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 배너 이미지 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배너 이미지 생성/수정 요청")
public class BannerImageRequest {

    @Schema(description = "배너 이미지 URL", example = "https://ckokservice.s3.ap-northeast-2.amazonaws.com/banners/banner1.jpg")
    @NotBlank(message = "배너 이미지 URL은 필수입니다")
    @Size(max = 1000, message = "배너 이미지 URL은 1000자를 초과할 수 없습니다")
    private String bannerUrl;

    @Schema(description = "클릭 시 이동할 URL", example = "https://example.com/campaign")
    @Size(max = 1000, message = "리다이렉트 URL은 1000자를 초과할 수 없습니다")
    private String redirectUrl;

    @Schema(description = "배너 제목 (선택사항)", example = "신규 캠페인 프로모션")
    @Size(max = 100, message = "배너 제목은 100자를 초과할 수 없습니다")
    private String title;

    @Schema(description = "배너 설명 (선택사항)", example = "새로운 캠페인을 확인해보세요!")
    @Size(max = 1000, message = "배너 설명은 1000자를 초과할 수 없습니다")
    private String description;

    @Schema(description = "배너 포지션 (선택사항)", example = "TOP", allowableValues = {"TOP", "MIDDLE", "BOTTOM", "SIDEBAR"})
    private String position;

    @Schema(description = "배너 표시 순서 (선택사항, 낮을수록 상위)", example = "1")
    private Integer displayOrder;
}