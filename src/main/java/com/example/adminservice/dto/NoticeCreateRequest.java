package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "공지사항 생성 요청")
public class NoticeCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "공지사항 제목", example = "중요한 공지사항입니다", required = true)
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Schema(description = "공지사항 내용", example = "공지사항 내용입니다...", required = true)
    private String content;

    @Schema(description = "필독 여부", example = "true", defaultValue = "false")
    private Boolean isMustRead = false;

    public NoticeCreateRequest(String title, String content, Boolean isMustRead) {
        this.title = title;
        this.content = content;
        this.isMustRead = isMustRead;
    }
}
