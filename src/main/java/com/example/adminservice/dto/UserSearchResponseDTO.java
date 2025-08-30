package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 검색 응답 DTO")
public class UserSearchResponseDTO {

    @Schema(description = "검색 결과")
    private List<UserInfo> users;

    @Schema(description = "페이지 정보")
    private PageInfo pagination;

    @Schema(description = "검색 통계")
    private SearchStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "닉네임", example = "김사용자")
        private String nickname;

        @Schema(description = "사용자 권한", example = "USER")
        private String role;

        @Schema(description = "소셜 로그인 제공자", example = "GOOGLE")
        private String provider;

        @Schema(description = "계정 타입", example = "SOCIAL")
        private String accountType;

        @Schema(description = "활성화 여부", example = "true")
        private Boolean active;

        @Schema(description = "이메일 인증 여부", example = "true")
        private Boolean emailVerified;

        @Schema(description = "성별", example = "MALE")
        private String gender;

        @Schema(description = "나이", example = "28")
        private Integer age;

        @Schema(description = "전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImg;

        @Schema(description = "가입일", example = "2025-07-14T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "최종 수정일", example = "2025-07-14T15:30:00")
        private LocalDateTime updatedAt;

        @Schema(description = "관리자 메모", example = "VIP 고객")
        private String memo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지 정보")
    public static class PageInfo {
        @Schema(description = "현재 페이지 번호", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지당 항목 수", example = "20")
        private Integer pageSize;

        @Schema(description = "전체 페이지 수", example = "5")
        private Integer totalPages;

        @Schema(description = "전체 항목 수", example = "100")
        private Long totalElements;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private Boolean hasPrevious;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "검색 통계")
    public static class SearchStats {
        @Schema(description = "총 검색 결과 수", example = "100")
        private Long totalCount;

        @Schema(description = "활성 사용자 수", example = "95")
        private Long activeCount;

        @Schema(description = "비활성 사용자 수", example = "5")
        private Long inactiveCount;

        @Schema(description = "이메일 인증 완료 사용자 수", example = "90")
        private Long verifiedCount;

        @Schema(description = "역할별 통계")
        private RoleStats roleStats;

        @Schema(description = "제공자별 통계")
        private ProviderStats providerStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "역할별 통계")
    public static class RoleStats {
        @Schema(description = "일반 사용자 수", example = "80")
        private Long userCount;

        @Schema(description = "클라이언트 수", example = "18")
        private Long clientCount;

        @Schema(description = "관리자 수", example = "2")
        private Long adminCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "제공자별 통계")
    public static class ProviderStats {
        @Schema(description = "Google 로그인 사용자 수", example = "40")
        private Long googleCount;

        @Schema(description = "Kakao 로그인 사용자 수", example = "35")
        private Long kakaoCount;

        @Schema(description = "Naver 로그인 사용자 수", example = "20")
        private Long naverCount;

        @Schema(description = "로컬 계정 사용자 수", example = "5")
        private Long localCount;
    }
}
