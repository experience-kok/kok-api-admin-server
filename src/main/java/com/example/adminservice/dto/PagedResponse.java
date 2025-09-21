package com.example.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
    }
    
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pagination(PaginationInfo.builder()
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .build();
    }
}
