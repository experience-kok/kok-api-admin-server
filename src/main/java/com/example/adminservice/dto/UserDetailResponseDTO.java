package com.example.adminservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponseDTO {
    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String provider;
    private String accountType;
    private Boolean active;
    private Boolean emailVerified;
    private String gender;
    private Integer age;
    private String phone;
    private String profileImg;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginDate;
    private Integer loginCount;
    
    // 추가 상세 정보
    private List<CampaignSummaryDTO> campaigns;
    private List<ApplicationSummaryDTO> applications;
    private StatisticsDTO statistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignSummaryDTO {
        private Long id;
        private String title;
        private String status;
        private String approvalStatus;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationSummaryDTO {
        private Long id;
        private String campaignTitle;
        private String status;
        private LocalDateTime appliedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsDTO {
        private Long totalCampaigns;
        private Long totalApplications;
        private Long approvedCampaigns;
        private Long rejectedCampaigns;
    }
}
