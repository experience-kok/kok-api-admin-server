package com.example.adminservice.dto;

import com.example.adminservice.domain.Markdown;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "마크다운 상세 응답")
public class MarkdownDetailResponse {

    @Schema(description = "마크다운 ID", example = "1")
    private Long id;

    @Schema(description = "마크다운 제목", example = "Spring Boot 시작하기")
    private String title;

    @Schema(description = "마크다운 내용", example = "# Spring Boot 가이드\n\nSpring Boot는...\n\n## 설치 방법\n...")
    private String content;

    @Schema(description = "조회수", example = "157")
    private Long viewCount;

    @Schema(description = "작성자 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "개발자")
    private String authorName;

    @Schema(description = "생성 시간", example = "2025-08-26T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-08-26T14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static MarkdownDetailResponse from(Markdown markdown) {
        return MarkdownDetailResponse.builder()
                .id(markdown.getId())
                .title(markdown.getTitle())
                .content(markdown.getContent())
                .viewCount(markdown.getViewCount())
                .authorId(markdown.getAuthorId())
                .authorName(markdown.getAuthorName())
                .createdAt(markdown.getCreatedAt())
                .updatedAt(markdown.getUpdatedAt())
                .build();
    }
}
