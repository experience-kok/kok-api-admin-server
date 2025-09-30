package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "콕포스트 생성 요청")
public class KokPostCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "글 제목", example = "맛집 체험 후기", required = true)
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Schema(description = "글 내용", example = "이곳은 정말 맛있는 식당입니다...", required = true)
    private String content;

    @Positive(message = "캠페인 ID는 양수여야 합니다")
    @Schema(description = "캠페인 ID (활성화 상태일 때 필수)", example = "1", required = false)
    private Long campaignId;

    @NotNull(message = "활성 여부는 필수입니다")
    @Schema(description = "활성 여부 (true: 활성화, false: 비활성화) - 활성화 시 campaignId 필수", 
            example = "true", required = true)
    private Boolean active;

    @NotNull(message = "방문 정보는 필수입니다")
    @Valid
    @Schema(description = "방문 정보 (연락처는 필수, 나머지는 선택)", required = true)
    private KokPostVisitInfoDto visitInfo;

    public KokPostCreateRequest(String title, String content, Long campaignId, Boolean active, KokPostVisitInfoDto visitInfo) {
        this.title = title;
        this.content = content;
        this.campaignId = campaignId;
        this.active = active;
        this.visitInfo = visitInfo;
    }

    /**
     * 활성화 상태일 때 campaignId가 있는지 검증
     */
    public void validateCampaignIdForActive() {
        if (Boolean.TRUE.equals(active) && campaignId == null) {
            throw new IllegalArgumentException("활성화 상태로 생성하려면 캠페인 ID가 필요합니다.");
        }
    }
}
