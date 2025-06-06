package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@Schema(description = "사용자 상세 정보")
public class UserDetailDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "사용자 권한", example = "USER")
    private String userRole;

    @Schema(description = "생성일시", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "연동된 SNS 플랫폼 목록")
    private List<PlatformInfo> platforms;

    @Getter
    @ToString
    @AllArgsConstructor
    @Schema(description = "SNS 플랫폼 정보")
    public static class PlatformInfo {

        @Schema(description = "플랫폼 ID", example = "1")
        private Long id;

        @Schema(description = "플랫폼 유형", example = "BLOG")
        private String platformType;

        @Schema(description = "계정 URL", example = "https://blog.naver.com/example")
        private String accountUrl;

        @Schema(description = "팔로워 수", example = "1000")
        private Integer followerCount;

        @Schema(description = "마지막 크롤링 일시", example = "2023-01-01T00:00:00")
        private LocalDateTime lastCrawledAt;
    }
}