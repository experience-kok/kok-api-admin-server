package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 배너 순서 변경 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배너 순서 변경 요청")
public class BannerOrderUpdateRequest {

    @Schema(description = "순서 변경할 배너 목록")
    @Valid
    @NotNull(message = "배너 목록은 필수입니다")
    private List<BannerOrderItem> banners;

    /**
     * 개별 배너 순서 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "배너 순서 정보")
    public static class BannerOrderItem {

        @Schema(description = "배너 ID", example = "1")
        @NotNull(message = "배너 ID는 필수입니다")
        @Positive(message = "배너 ID는 양수여야 합니다")
        private Long id;

        @Schema(description = "새로운 표시 순서 (낮을수록 상위)", example = "1")
        @NotNull(message = "표시 순서는 필수입니다")
        @Positive(message = "표시 순서는 양수여야 합니다")
        private Integer displayOrder;
    }
}
