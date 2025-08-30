package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "마크다운 목록 응답 래퍼")
public class MarkdownListResponseWrapper {

    @Schema(description = "마크다운 목록")
    private List<MarkdownListResponse> markdowns;

    public static MarkdownListResponseWrapper of(List<MarkdownListResponse> markdowns) {
        return MarkdownListResponseWrapper.builder()
                .markdowns(markdowns)
                .build();
    }
}
