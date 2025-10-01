package com.example.adminservice.dto;

import com.example.adminservice.domain.KokPost;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "콕포스트 상세 응답")
public class KokPostDetailResponse {

    @Schema(description = "콕포스트 ID", example = "1")
    private Long id;

    @Schema(description = "글 제목", example = "맛집 체험 후기")
    private String title;

    @Schema(description = "글 내용", example = "이곳은 정말 맛있는 식당입니다...")
    private String content;

    @Schema(description = "조회수", example = "157")
    private Long viewCount;

    @Schema(description = "캠페인 ID", example = "1")
    private Long campaignId;

    @Schema(description = "작성자 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "관리자")
    private String authorName;

    @Schema(description = "활성 여부", example = "true")
    private boolean active;

    @Schema(description = "방문 정보")
    private KokPostVisitInfoDto visitInfo;

    @Schema(description = "생성 시간", example = "2025-08-26T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-08-26T14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static KokPostDetailResponse from(KokPost kokPost) {
        return KokPostDetailResponse.builder()
                .id(kokPost.getId())
                .title(kokPost.getTitle())
                .content(kokPost.getContent())
                .viewCount(kokPost.getViewCount())
                .campaignId(kokPost.getCampaignId())
                .authorId(kokPost.getAuthorId())
                .authorName(kokPost.getAuthorName())
                .active(kokPost.isActive())
                .visitInfo(KokPostVisitInfoDto.from(kokPost.getVisitInfo()))
                .createdAt(kokPost.getCreatedAt())
                .updatedAt(kokPost.getUpdatedAt())
                .build();
    }
    
    /**
     * Markdown → HTML 변환된 content로 응답 생성
     */
    public static KokPostDetailResponse fromWithHtml(KokPost kokPost, String htmlContent) {
        return KokPostDetailResponse.builder()
                .id(kokPost.getId())
                .title(kokPost.getTitle())
                .content(htmlContent)  // HTML로 변환된 내용
                .viewCount(kokPost.getViewCount())
                .campaignId(kokPost.getCampaignId())
                .authorId(kokPost.getAuthorId())
                .authorName(kokPost.getAuthorName())
                .active(kokPost.isActive())
                .visitInfo(KokPostVisitInfoDto.from(kokPost.getVisitInfo()))
                .createdAt(kokPost.getCreatedAt())
                .updatedAt(kokPost.getUpdatedAt())
                .build();
    }
}
