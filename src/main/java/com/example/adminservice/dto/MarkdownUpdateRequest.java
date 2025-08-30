package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "마크다운 수정 요청")
public class MarkdownUpdateRequest {

    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    @Schema(description = "수정할 제목", example = "수정된 제목")
    private String title;

    @Schema(description = "수정할 내용", example = "# 수정된 내용\n\n새로운 마크다운...")
    private String content;

    public MarkdownUpdateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
