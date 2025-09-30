package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "콕포스트 수정 요청")
public class KokPostUpdateRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "수정할 제목", example = "수정된 맛집 후기")
    private String title;

    @Schema(description = "수정할 내용", example = "수정된 내용입니다...")
    private String content;

    @Schema(description = "수정할 캠페인 ID (null로 설정하면 캠페인 연결 해제)", example = "1")
    private Long campaignId;

    @Valid
    @Schema(description = "수정할 방문 정보 (연락처는 필수, 나머지는 선택)")
    private KokPostVisitInfoDto visitInfo;

    public KokPostUpdateRequest(String title, String content, Long campaignId, KokPostVisitInfoDto visitInfo) {
        this.title = title;
        this.content = content;
        this.campaignId = campaignId;
        this.visitInfo = visitInfo;
    }
}
