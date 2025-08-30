package com.example.adminservice.constant;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum SortOption {
    LATEST("latest", "최신순", Sort.by(Sort.Direction.DESC, "createdAt")),
    VIEW_COUNT_DESC("viewCountDesc", "조회수 높은순", Sort.by(Sort.Direction.DESC, "viewCount")),
    VIEW_COUNT_ASC("viewCountAsc", "조회수 낮은순", Sort.by(Sort.Direction.ASC, "viewCount"));

    private final String value;
    private final String description;
    private final Sort sort;

    SortOption(String value, String description, Sort sort) {
        this.value = value;
        this.description = description;
        this.sort = sort;
    }

    public static SortOption fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LATEST; // 기본값
        }
        
        for (SortOption option : SortOption.values()) {
            if (option.value.equalsIgnoreCase(value.trim())) {
                return option;
            }
        }
        return LATEST; // 알 수 없는 값이면 기본값
    }
}
