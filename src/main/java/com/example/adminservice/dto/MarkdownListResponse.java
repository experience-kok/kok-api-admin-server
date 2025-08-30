package com.example.adminservice.dto;

import com.example.adminservice.domain.Markdown;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "마크다운 목록 응답")
public class MarkdownListResponse {

    @Schema(description = "마크다운 ID", example = "1")
    private Long id;

    @Schema(description = "마크다운 제목", example = "Spring Boot 완전 가이드")
    private String title;

    @Schema(description = "조회수", example = "156")
    private Long viewCount;

    @Schema(description = "작성자 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "개발자")
    private String authorName;

    @Schema(description = "생성 시간", example = "2025-08-26T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-08-26T15:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static MarkdownListResponse from(Markdown markdown) {
        return MarkdownListResponse.builder()
                .id(markdown.getId())
                .title(markdown.getTitle())
                .viewCount(markdown.getViewCount())
                .authorId(markdown.getAuthorId())
                .authorName(markdown.getAuthorName())
                .createdAt(markdown.getCreatedAt())
                .updatedAt(markdown.getUpdatedAt())
                .build();
    }
}
