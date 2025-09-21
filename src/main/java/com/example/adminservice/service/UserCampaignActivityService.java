package com.example.adminservice.service;

import com.example.adminservice.constant.ApplicationStatus;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.Campaign;
import com.example.adminservice.domain.CampaignApplication;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.ActivityItemDto;
import com.example.adminservice.dto.UserCampaignActivityDto;
import com.example.adminservice.repository.CampaignApplicationRepository;
import com.example.adminservice.repository.CampaignRepository;
import com.example.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 사용자 캠페인 활동 내역 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCampaignActivityService {

    private final UserRepository userRepository;
    private final CampaignApplicationRepository campaignApplicationRepository;
    private final CampaignRepository campaignRepository;

    /**
     * 사용자의 캠페인 활동 내역 조회
     */
    public UserCampaignActivityDto getUserCampaignActivities(Long userId, Pageable pageable, String statusFilter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        if (user.getRole() == UserRole.USER) {
            return buildUserApplicationActivity(user, pageable, statusFilter);
        } else if (user.getRole() == UserRole.CLIENT) {
            return buildClientCampaignActivity(user, pageable, statusFilter);
        } else {
            throw new RuntimeException("지원하지 않는 사용자 타입입니다: " + user.getRole());
        }
    }

    /**
     * USER 타입 - 캠페인 신청 내역 구성
     */
    private UserCampaignActivityDto buildUserApplicationActivity(User user, Pageable pageable, String statusFilter) {
        log.debug("USER 타입 활동 내역 조회: userId={}, statusFilter={}", user.getId(), statusFilter);

        // 신청 내역 조회
        Page<CampaignApplication> applicationsPage;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            try {
                ApplicationStatus status = ApplicationStatus.valueOf(statusFilter.toUpperCase());
                applicationsPage = campaignApplicationRepository.findByUserIdAndStatus(user.getId(), status, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 상태입니다: " + statusFilter);
            }
        } else {
            applicationsPage = campaignApplicationRepository.findByUserIdWithCampaignInfo(user.getId(), pageable);
        }

        // DTO 변환
        List<ActivityItemDto> items = applicationsPage.getContent().stream()
                .map(this::convertToApplicationActivityItem)
                .collect(Collectors.toList());

        return UserCampaignActivityDto.builder()
                .userId(user.getId())
                .userRole(user.getRole().name())
                .items(items)
                .pagination(UserCampaignActivityDto.PaginationDto.from(applicationsPage))
                .build();
    }

    /**
     * CLIENT 타입 - 생성한 캠페인 내역 구성
     */
    private UserCampaignActivityDto buildClientCampaignActivity(User user, Pageable pageable, String statusFilter) {
        log.debug("CLIENT 타입 활동 내역 조회: userId={}, statusFilter={}", user.getId(), statusFilter);

        // 캠페인 내역 조회
        Page<Campaign> campaignsPage;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            try {
                Campaign.ApprovalStatus status = Campaign.ApprovalStatus.valueOf(statusFilter.toUpperCase());
                campaignsPage = campaignRepository.findByCreatorIdAndApprovalStatus(user.getId(), status, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 상태입니다: " + statusFilter);
            }
        } else {
            campaignsPage = campaignRepository.findByCreatorIdWithCompanyInfo(user.getId(), pageable);
        }

        // 캠페인별 신청자 수 조회
        List<Long> campaignIds = campaignsPage.getContent().stream()
                .map(Campaign::getId)
                .collect(Collectors.toList());
        
        Map<Long, Integer> applicationCounts = getApplicationCounts(campaignIds);

        // DTO 변환
        List<ActivityItemDto> items = campaignsPage.getContent().stream()
                .map(campaign -> convertToCampaignActivityItem(campaign, applicationCounts.get(campaign.getId())))
                .collect(Collectors.toList());

        return UserCampaignActivityDto.builder()
                .userId(user.getId())
                .userRole(user.getRole().name())
                .items(items)
                .pagination(UserCampaignActivityDto.PaginationDto.from(campaignsPage))
                .build();
    }

    /**
     * 캠페인 신청을 ActivityItemDto로 변환
     */
    private ActivityItemDto convertToApplicationActivityItem(CampaignApplication application) {
        Campaign campaign = application.getCampaign();
        String companyName = campaign.getCompany() != null ? campaign.getCompany().getCompanyName() : "미등록";

        return ActivityItemDto.builder()
                .id(application.getId())
                .title(campaign.getTitle())
                .company(companyName)
                .type(campaign.getCampaignType())
                .status(application.getApplicationStatus().name())
                .statusText(application.getApplicationStatus().getDescription())
                .createdAt(application.getAppliedAt().toLocalDateTime())
                .updatedAt(application.getUpdatedAt().toLocalDateTime())
                .campaignId(campaign.getId())
                .maxApplicants(campaign.getMaxApplicants())
                .build();
    }

    /**
     * 캠페인을 ActivityItemDto로 변환
     */
    private ActivityItemDto convertToCampaignActivityItem(Campaign campaign, Integer currentApplications) {
        String companyName = campaign.getCompany() != null ? campaign.getCompany().getCompanyName() : "미등록";
        String approvedBy = campaign.getApprovedBy() != null ? campaign.getApprovedBy().getNickname() : null;
        
        // 모집 기간 문자열 생성
        String recruitmentPeriod = null;
        if (campaign.getRecruitmentStartDate() != null) {
            if (campaign.getRecruitmentEndDate() != null) {
                recruitmentPeriod = campaign.getRecruitmentStartDate() + " ~ " + campaign.getRecruitmentEndDate();
            } else {
                recruitmentPeriod = campaign.getRecruitmentStartDate() + " ~ 상시";
            }
        }

        return ActivityItemDto.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .company(companyName)
                .type(campaign.getCampaignType())
                .status(campaign.getApprovalStatus().name())
                .statusText(campaign.getApprovalStatus().getDescription())
                .createdAt(campaign.getCreatedAtAsLocalDateTime())
                .updatedAt(campaign.getUpdatedAtAsLocalDateTime())
                .currentApplications(currentApplications != null ? currentApplications : 0)
                .approvedBy(approvedBy)
                .approvalDate(campaign.getApprovalDateAsLocalDateTime())
                .recruitmentPeriod(recruitmentPeriod)
                .build();
    }

    /**
     * 캠페인별 신청자 수 조회
     */
    private Map<Long, Integer> getApplicationCounts(List<Long> campaignIds) {
        if (campaignIds.isEmpty()) {
            return new HashMap<>();
        }

        Object[][] results = campaignRepository.countApplicationsByCampaignIds(campaignIds);
        Map<Long, Integer> counts = new HashMap<>();
        
        for (Object[] result : results) {
            Long campaignId = (Long) result[0];
            Long count = (Long) result[1];
            counts.put(campaignId, count.intValue());
        }

        // 신청자가 없는 캠페인은 0으로 설정
        for (Long campaignId : campaignIds) {
            counts.putIfAbsent(campaignId, 0);
        }

        return counts;
    }
}
