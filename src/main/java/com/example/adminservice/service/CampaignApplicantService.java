package com.example.adminservice.service;

import com.example.adminservice.constant.ApplicationStatus;
import com.example.adminservice.domain.Campaign;
import com.example.adminservice.domain.CampaignApplication;
import com.example.adminservice.dto.CampaignApplicantDto;
import com.example.adminservice.dto.CampaignApplicantListResponse;
import com.example.adminservice.repository.CampaignApplicationRepository;
import com.example.adminservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 캠페인 신청자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignApplicantService {

    private final CampaignRepository campaignRepository;
    private final CampaignApplicationRepository campaignApplicationRepository;

    /**
     * 특정 캠페인의 신청자 목록 조회 (페이징)
     */
    public CampaignApplicantListResponse getCampaignApplicants(Long campaignId, int page, int size, String status) {
        // 페이지 번호와 크기 검증
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }

        // 캠페인 존재 여부 확인
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("캠페인을 찾을 수 없습니다: " + campaignId));

        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignApplication> applicationsPage;

        // 상태 필터링
        if (status != null && !status.trim().isEmpty()) {
            ApplicationStatus applicationStatus = parseApplicationStatus(status.trim());
            applicationsPage = campaignApplicationRepository.findByCampaignIdAndStatus(campaignId, applicationStatus, pageable);
        } else {
            applicationsPage = campaignApplicationRepository.findByCampaignIdWithUserInfo(campaignId, pageable);
        }

        // 총 신청자 수 조회
        long totalApplicants = campaignApplicationRepository.countByCampaignId(campaignId);

        // DTO 변환
        List<CampaignApplicantDto> applicants = applicationsPage.getContent().stream()
                .map(this::convertToApplicantDto)
                .collect(Collectors.toList());

        return CampaignApplicantListResponse.builder()
                .campaignId(campaignId)
                .campaignTitle(campaign.getTitle())
                .totalApplicants(totalApplicants)
                .applicants(applicants)
                .pagination(CampaignApplicantListResponse.PaginationDto.from(applicationsPage))
                .build();
    }

    /**
     * 문자열을 ApplicationStatus enum으로 변환
     */
    private ApplicationStatus parseApplicationStatus(String status) {
        try {
            // 한글 상태명으로 변환
            switch (status) {
                case "신청":
                    return ApplicationStatus.APPLIED;
                case "선정 대기중":
                case "대기중":
                    return ApplicationStatus.PENDING;
                case "선정":
                case "선정됨":
                    return ApplicationStatus.SELECTED;
                case "거절":
                case "거절됨":
                    return ApplicationStatus.REJECTED;
                case "완료":
                case "완료됨":
                    return ApplicationStatus.COMPLETED;
                default:
                    // 영문 상태명으로 시도
                    return ApplicationStatus.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 신청 상태입니다: " + status + 
                ". 가능한 값: 신청, 선정 대기중, 선정, 거절, 완료");
        }
    }

    /**
     * CampaignApplication을 CampaignApplicantDto로 변환
     */
    private CampaignApplicantDto convertToApplicantDto(CampaignApplication application) {
        return CampaignApplicantDto.builder()
                .id(application.getUser().getId())
                .nickname(application.getUser().getNickname())
                .email(application.getUser().getEmail())
                .role(application.getUser().getRole().name())
                .active(application.getUser().getActive())
                .appliedAt(application.getAppliedAt())
                .applicationStatus(application.getApplicationStatus().name())
                .statusText(application.getApplicationStatus().getDescription())
                .build();
    }

}
