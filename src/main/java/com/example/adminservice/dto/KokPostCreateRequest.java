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

    @NotNull(message = "캠페인 ID는 필수입니다")
    @Positive(message = "캠페인 ID는 양수여야 합니다")
    @Schema(description = "캠페인 ID", example = "1", required = true)
    private Long campaignId;

    @NotNull(message = "방문 정보는 필수입니다")
    @Valid
    @Schema(description = "방문 정보 (연락처는 필수, 나머지는 선택)", required = true)
    private KokPostVisitInfoDto visitInfo;

    public KokPostCreateRequest(String title, String content, Long campaignId, KokPostVisitInfoDto visitInfo) {
        this.title = title;
        this.content = content;
        this.campaignId = campaignId;
        this.visitInfo = visitInfo;
    }
}
