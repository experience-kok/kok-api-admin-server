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

    @Schema(description = "페이지네이션 정보")
    private PaginationInfo pagination;

    @Getter
    @Builder
    @Schema(description = "페이지네이션 정보")
    public static class PaginationInfo {
        
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int pageNumber;

        @Schema(description = "페이지 크기", example = "10")
        private int pageSize;

        @Schema(description = "전체 페이지 수", example = "5")
        private int totalPages;

        @Schema(description = "전체 항목 수", example = "50")
        private long totalElements;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;
    }

    public static NoticePageResponse from(Page<NoticeListResponse> page) {
        PaginationInfo paginationInfo = PaginationInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return NoticePageResponse.builder()
                .notices(page.getContent())
                .pagination(paginationInfo)
                .build();
    }
}
