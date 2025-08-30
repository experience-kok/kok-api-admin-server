package com.example.adminservice.dto;

import com.example.adminservice.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * 승인 대기 캠페인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "승인 대기 캠페인 정보")
public class PendingCampaignResponse {

    @Schema(description = "캠페인 ID", example = "123")
    private Long id;

    @Schema(description = "캠페인 제목", example = "인스타 감성 카페 체험단 모집")
    private String title;

    @Schema(description = "캠페인 타입", example = "인스타그램")
    private String campaignType;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/cafe.jpg")
    private String thumbnailUrl;

    @Schema(description = "제품 간단 정보", example = "시그니처 음료 2잔 + 디저트 1개 무료 제공")
    private String productShortInfo;

    @Schema(description = "최대 신청자 수", example = "10")
    private Integer maxApplicants;

    @Schema(description = "모집 시작일", example = "2025-08-01")
    private LocalDate recruitmentStartDate;

    @Schema(description = "모집 종료일", example = "2025-08-15")
    private LocalDate recruitmentEndDate;


    @Schema(description = "선정일", example = "2025-08-16")
    private LocalDate selectionDate;

    @Schema(description = "리뷰 마감일", example = "2025-08-30")
    private LocalDate reviewDeadlineDate;

    @Schema(description = "승인 상태", example = "대기중")
    private String approvalStatus;

    @Schema(description = "승인/거절 코멘트", example = "승인 검토 중입니다.")
    private String approvalComment;

    @Schema(description = "승인 처리 일시", example = "2025-07-14T15:30:00")
    private LocalDateTime approvalDate;

    @Schema(description = "캠페인 생성 일시", example = "2025-07-14T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "승인자 정보 (승인된 경우에만 존재)")
    private ApproverInfo approver;

    @Schema(description = "캠페인 생성자 정보")
    private CreatorInfo creator;

    @Schema(description = "회사 정보")
    private CompanyInfo company;

    @Schema(description = "캠페인 위치 정보")
    private LocationInfo location;

    @Schema(description = "캠페인 미션 정보")
    private MissionInfo missionInfo;


    /**
     * 승인자 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "승인자 정보")
    public static class ApproverInfo {
        @Schema(description = "승인자 ID", example = "1")
        private Long id;

        @Schema(description = "승인자 이름", example = "김관리자")
        private String nickname;

        @Schema(description = "승인자 이메일", example = "admin@example.com")
        private String email;
    }

    /**
     * 캠페인 생성자 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "캠페인 생성자 정보")
    public static class CreatorInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "사용자 이름", example = "김클라이언트")
        private String nickname;

        @Schema(description = "이메일", example = "client@example.com")
        private String email;

        @Schema(description = "계정 타입", example = "SOCIAL")
        private String accountType;

        @Schema(description = "사용자 권한", example = "CLIENT")
        private String role;
    }

    /**
     * 회사 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회사 정보")
    public static class CompanyInfo {
        @Schema(description = "회사 ID", example = "1")
        private Long id;

        @Schema(description = "회사명", example = "맛있는 카페")
        private String companyName;

        @Schema(description = "사업자등록번호", example = "123-45-67890")
        private String businessRegistrationNumber;

        @Schema(description = "담당자명", example = "김담당")
        private String contactPerson;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phoneNumber;
    }

    /**
     * 캠페인 위치 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "캠페인 위치 정보")
    public static class LocationInfo {
        @Schema(description = "위치 ID", example = "1")
        private Long id;

        @Schema(description = "위도", example = "37.5665")
        private Double latitude;

        @Schema(description = "경도", example = "126.9780")
        private Double longitude;

        @Schema(description = "사업장 주소", example = "서울특별시 강남구 테헤란로 123")
        private String businessAddress;

        @Schema(description = "사업장 상세 주소", example = "A동 1층 101호")
        private String businessDetailAddress;

        @Schema(description = "공식 홈페이지", example = "https://cafe.example.com")
        private String homepage;

        @Schema(description = "연락처", example = "02-123-4567")
        private String contactPhone;

        @Schema(description = "방문 및 예약 안내", example = "평일 10:00-22:00 운영, 예약 필수")
        private String visitAndReservationInfo;

        @Schema(description = "좌표 존재 여부", example = "true")
        private Boolean hasCoordinates;
    }

    /**
     * 캠페인 미션 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "캠페인 미션 정보")
    public static class MissionInfo {
        @Schema(description = "미션 정보 ID", example = "1")
        private Long id;

        @Schema(description = "제목 키워드 목록 (상시 캠페인일 경우 null 가능)", example = "[\"맛집\", \"인스타그램\"]")
        private String[] titleKeywords;

        @Schema(description = "본문 키워드 목록", example = "[\"체험\", \"리뷰\", \"추천\"]")
        private String[] bodyKeywords;

        @Schema(description = "필요한 영상 개수", example = "1")
        private Integer numberOfVideo;

        @Schema(description = "필요한 이미지 개수", example = "5")
        private Integer numberOfImage;

        @Schema(description = "필요한 글자 수", example = "300")
        private Integer numberOfText;

        @Schema(description = "지도 포함 여부", example = "true")
        private Boolean isMap;

        @Schema(description = "미션 가이드", example = "카페 방문 후 인스타그램에 리뷰 포스팅해주세요")
        private String missionGuide;

        @Schema(description = "미션 시작일", example = "2025-08-17")
        private LocalDate missionStartDate;

        @Schema(description = "미션 마감일", example = "2025-08-30")
        private LocalDate missionDeadlineDate;

        @Schema(description = "미션 생성 일시", example = "2025-07-14T15:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "미션 수정 일시", example = "2025-07-14T15:30:00")
        private LocalDateTime updatedAt;
    }
}
