package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "아이디와 제목만 제공하는 캠페인 정보")
public class ShortCampaignResponse {
    @Schema(description = "캠페인 ID", example = "125")
    private Long id;

    @Schema(description = "캠페인 제목", example = "운동화 리뷰 캠페인")
    private String title;
}
