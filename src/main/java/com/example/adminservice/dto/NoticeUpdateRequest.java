package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "공지사항 수정 요청")
public class NoticeUpdateRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "수정할 제목", example = "수정된 공지사항 제목")
    private String title;

    @Schema(description = "수정할 내용", example = "수정된 공지사항 내용...")
    private String content;

    @Schema(description = "필독 여부", example = "true")
    private Boolean isMustRead;

    public NoticeUpdateRequest(String title, String content, Boolean isMustRead) {
        this.title = title;
        this.content = content;
        this.isMustRead = isMustRead;
    }
}
