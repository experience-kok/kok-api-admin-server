package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "공지사항 페이지 응답")
public class NoticePageResponse {

    @Schema(description = "공지사항 목록")
    private List<NoticeListResponse> notices;

    @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "현재 페이지 항목 수", example = "10")
    private int size;

    @Schema(description = "전체 항목 수", example = "48")
    private long totalElements;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;

    @Schema(description = "빈 페이지 여부", example = "false")
    private boolean empty;

    public static NoticePageResponse from(Page<NoticeListResponse> page) {
        return NoticePageResponse.builder()
                .notices(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
