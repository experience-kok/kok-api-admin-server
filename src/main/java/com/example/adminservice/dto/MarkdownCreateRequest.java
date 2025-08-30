package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "마크다운 생성 요청")
public class MarkdownCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    @Schema(description = "마크다운 제목", example = "Spring Boot 개발 가이드", required = true)
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Schema(description = "마크다운 내용", example = "# Spring Boot\n\n## 기본 설정\n...", required = true)
    private String content;

    public MarkdownCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
