package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 검색 요청 DTO")
public class UserSearchRequestDTO {

    @Schema(description = "검색 키워드 (이메일, 닉네임, 전화번호, 메모)", example = "김")
    private String keyword;

    @Schema(description = "사용자 권한 (USER, CLIENT, ADMIN)", example = "USER")
    private String role;

    @Schema(description = "계정 타입 (SOCIAL, LOCAL)", example = "SOCIAL")
    private String accountType;

    @Schema(description = "소셜 로그인 제공자 (GOOGLE, KAKAO, NAVER, LOCAL)", example = "GOOGLE")
    private String provider;

    @Schema(description = "활성화 상태", example = "true")
    private Boolean active;

    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean emailVerified;

    @Schema(description = "성별 (MALE, FEMALE, OTHER)", example = "MALE")
    private String gender;

    @Schema(description = "최소 나이", example = "20")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    private Integer minAge;

    @Schema(description = "최대 나이", example = "65")
    @Max(value = 120, message = "나이는 120 이하여야 합니다")
    private Integer maxAge;

    @Schema(description = "검색 시작일 (가입일 기준)", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "검색 종료일 (가입일 기준)", example = "2025-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지당 항목 수", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "정렬 기준 (createdAt, updatedAt, email, nickname)", example = "createdAt")
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향 (asc, desc)", example = "desc")
    @Builder.Default
    private String sortDirection = "desc";
}
