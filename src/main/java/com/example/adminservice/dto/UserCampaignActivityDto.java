package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 사용자 캠페인 활동 내역 응답 DTO
 */
@Data
@Builder
@Schema(description = "사용자 캠페인 활동 내역 응답")
public class UserCampaignActivityDto {
    
    @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    @Schema(description = "사용자 권한", example = "USER", allowableValues = {"USER", "CLIENT", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String userRole;
    
    @Schema(description = "활동 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ActivityItemDto> items;
    
    @Schema(description = "페이지네이션 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaginationDto pagination;

    @Data
    @Builder
    @Schema(description = "페이지네이션 정보")
    public static class PaginationDto {
        
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private int pageNumber;
        
        @Schema(description = "페이지당 항목 수", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        private int pageSize;
        
        @Schema(description = "전체 페이지 수", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        private int totalPages;
        
        @Schema(description = "전체 항목 수", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
        private long totalElements;
        
        @Schema(description = "첫 페이지 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        private boolean first;
        
        @Schema(description = "마지막 페이지 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        private boolean last;

        public static PaginationDto from(Page<?> page) {
            return PaginationDto.builder()
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .build();
        }
    }
}
