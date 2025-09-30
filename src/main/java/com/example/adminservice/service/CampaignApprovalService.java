package com.example.adminservice.service;

import com.example.adminservice.domain.*;
import com.example.adminservice.dto.CampaignApprovalRequest;
import com.example.adminservice.dto.CampaignApprovalResponse;
import com.example.adminservice.dto.PendingCampaignResponse;
import com.example.adminservice.dto.SimpleCampaignResponse;
import com.example.adminservice.repository.CampaignLocationRepository;
import com.example.adminservice.repository.CampaignRepository;
import com.example.adminservice.repository.CompanyRepository;
import com.example.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 캠페인 승인 관리 서비스 (새로운 엔티티 구조 대응)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignApprovalService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CampaignLocationRepository campaignLocationRepository;
    private final ClientNotificationService clientNotificationService;
    private final SESService sesService;
    private final KokPostService kokPostService;  // KokPostService 의존성 추가

    /**
     * 승인 대기 중인 캠페인 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PendingCampaignResponse> getPendingCampaigns(int page, int size) {
        log.info("승인 대기 캠페인 목록 조회: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Campaign> campaigns = campaignRepository.findByApprovalStatus(
                Campaign.ApprovalStatus.PENDING, pageable);

        return campaigns.map(this::convertToPendingCampaignResponse);
    }

    /**
     * 모든 캠페인 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Page<PendingCampaignResponse> getAllCampaigns(int page, int size, String approvalStatus) {
        log.info("전체 캠페인 목록 조회: page={}, size={}, approvalStatus={}", page, size, approvalStatus);

        if (approvalStatus != null && "EXPIRED".equalsIgnoreCase(approvalStatus)) {
            return getExpiredCampaigns(page, size);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Campaign> campaigns;

        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            try {
                // 한글 입력을 영문으로 변환
                String englishStatus = convertKoreanToEnglishStatus(approvalStatus);
                Campaign.ApprovalStatus status = Campaign.ApprovalStatus.valueOf(englishStatus.toUpperCase());
                campaigns = campaignRepository.findByApprovalStatus(status, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 승인 상태입니다: " + approvalStatus + ". 사용 가능한 값: 대기중, 승인됨, 거절됨, 만료됨");
            }
        } else {
            campaigns = campaignRepository.findAll(pageable);
        }

        return campaigns.map(this::convertToPendingCampaignResponse);
    }

    /**
     * 만료된 캠페인 목록 조회
     */
    private Page<PendingCampaignResponse> getExpiredCampaigns(int page, int size) {
        Pageable allPageable = PageRequest.of(0, 1000, Sort.by("createdAt").descending());
        Page<Campaign> allCampaigns = campaignRepository.findAll(allPageable);

        LocalDate currentDate = LocalDate.now();

        List<PendingCampaignResponse> expiredCampaigns = allCampaigns.getContent().stream()
                .filter(campaign -> campaign.getRecruitmentEndDate() != null &&
                        campaign.getRecruitmentEndDate().isBefore(currentDate))
                .map(campaign -> {
                    PendingCampaignResponse response = convertToPendingCampaignResponse(campaign);
                    return PendingCampaignResponse.builder()
                            .id(response.getId())
                            .title(response.getTitle())
                            .campaignType(response.getCampaignType())
                            .thumbnailUrl(response.getThumbnailUrl())
                            .productShortInfo(response.getProductShortInfo())
                            .maxApplicants(response.getMaxApplicants())
                            .recruitmentStartDate(response.getRecruitmentStartDate())
                            .recruitmentEndDate(response.getRecruitmentEndDate())
                            .selectionDate(response.getSelectionDate())
                            .reviewDeadlineDate(response.getReviewDeadlineDate())
                            .approvalStatus("만료됨")
                            .approvalComment(response.getApprovalComment())
                            .approvalDate(response.getApprovalDate())
                            .createdAt(response.getCreatedAt())
                            .approver(response.getApprover()) // 승인자 정보 포함
                            .creator(response.getCreator())
                            .company(response.getCompany())
                            .location(response.getLocation())
                            .build();
                })
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, expiredCampaigns.size());
        List<PendingCampaignResponse> pagedExpiredCampaigns = start >= expiredCampaigns.size() ?
                List.of() : expiredCampaigns.subList(start, end);

        return new PageImpl<>(pagedExpiredCampaigns, PageRequest.of(page, size), expiredCampaigns.size());
    }

    /**
     * 캠페인 승인/거절 처리 (새로운 엔티티 구조 대응)
     */
    public CampaignApprovalResponse approveCampaign(String adminEmail, Long campaignId, CampaignApprovalRequest request) {
        log.info("캠페인 승인/거절 처리: adminEmail={}, campaignId={}, status={}",
                adminEmail, campaignId, request.getApprovalStatus());

        // 관리자 권한 확인
        User admin = userRepository.findByEmailAndRole(adminEmail,
                        com.example.adminservice.constant.UserRole.ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다"));

        if (!isAdmin(admin)) {
            throw new RuntimeException("관리자 권한이 필요합니다");
        }

        // 캠페인 조회
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("캠페인을 찾을 수 없습니다: " + campaignId));

        // 이미 승인/거절된 캠페인 체크
        if (campaign.getApprovalStatus() != Campaign.ApprovalStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 캠페인입니다: " + campaign.getApprovalStatus());
        }

        // 승인 상태 업데이트
        Campaign.ApprovalStatus newStatus;
        try {
            newStatus = Campaign.ApprovalStatus.valueOf(request.getApprovalStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 승인 상태입니다: " + request.getApprovalStatus());
        }

        if (newStatus == Campaign.ApprovalStatus.APPROVED) {
            // 새로운 구조: User 객체를 직접 전달
            campaign.approve(admin, request.getComment());

            // 캠페인 승인 알림 전송 (WebSocket/앱 내 알림)
            try {
                clientNotificationService.sendCampaignApprovalNotification(
                        campaign.getCreatorId(), // 호환성 메서드 사용
                        campaign.getId(),
                        campaign.getTitle(),
                        admin.getId()
                );
                log.info("캠페인 승인 알림 전송 요청 완료: campaignId={}", campaign.getId());
            } catch (Exception e) {
                log.error("캠페인 승인 알림 전송 실패: campaignId={}, error={}",
                        campaign.getId(), e.getMessage(), e);
            }

            // 캠페인 승인 이메일 발송
            try {
                User creator = campaign.getCreator();
                if (creator != null && creator.getEmail() != null) {
                    sesService.sendCampaignApprovedEmailSafe(
                            creator.getEmail(),
                            creator.getNickname() != null ? creator.getNickname() : "고객",
                            campaign.getTitle()
                    );
                    log.info("캠페인 승인 이메일 발송 요청 완료: campaignId={}, email={}",
                            campaign.getId(), creator.getEmail());
                } else {
                    log.warn("캠페인 승인 이메일 발송 실패 - 생성자 정보 또는 이메일 없음: campaignId={}",
                            campaign.getId());
                }
            } catch (Exception e) {
                log.error("캠페인 승인 이메일 발송 실패: campaignId={}, error={}",
                        campaign.getId(), e.getMessage(), e);
            }
        } else if (newStatus == Campaign.ApprovalStatus.REJECTED) {
            // 새로운 구조: User 객체를 직접 전달
            campaign.reject(admin, request.getComment());

            // 캠페인 거절 알림 전송 (WebSocket/앱 내 알림)
            try {
                clientNotificationService.sendCampaignRejectionNotification(
                        campaign.getCreatorId(), // 호환성 메서드 사용
                        campaign.getId(),
                        campaign.getTitle(),
                        request.getComment() != null ? request.getComment() : "승인 기준을 충족하지 않습니다.",
                        admin.getId()
                );
                log.info("캠페인 거절 알림 전송 요청 완료: campaignId={}", campaign.getId());
            } catch (Exception e) {
                log.error("캠페인 거절 알림 전송 실패: campaignId={}, error={}",
                        campaign.getId(), e.getMessage(), e);
            }

            // 캠페인 거절 이메일 발송
            try {
                User creator = campaign.getCreator();
                if (creator != null && creator.getEmail() != null) {
                    sesService.sendCampaignRejectedEmailSafe(
                            creator.getEmail(),
                            creator.getNickname() != null ? creator.getNickname() : "고객",
                            campaign.getTitle(),
                            request.getComment() != null ? request.getComment() : "승인 기준을 충족하지 않습니다."
                    );
                    log.info("캠페인 거절 이메일 발송 요청 완료: campaignId={}, email={}",
                            campaign.getId(), creator.getEmail());
                } else {
                    log.warn("캠페인 거절 이메일 발송 실패 - 생성자 정보 또는 이메일 없음: campaignId={}",
                            campaign.getId());
                }
            } catch (Exception e) {
                log.error("캠페인 거절 이메일 발송 실패: campaignId={}, error={}",
                        campaign.getId(), e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("APPROVED 또는 REJECTED만 허용됩니다");
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("캠페인 승인/거절 처리 완료: campaignId={}, status={}", campaignId, newStatus);

        return convertToCampaignApprovalResponse(savedCampaign, admin);
    }

    /**
     * 캠페인 상세 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public PendingCampaignResponse getCampaignDetail(Long campaignId) {
        log.info("캠페인 상세 조회 (관리자): campaignId={}", campaignId);

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("캠페인을 찾을 수 없습니다: " + campaignId));

        return convertToPendingCampaignResponse(campaign);
    }

    /**
     * 캠페인 검색
     */
    @Transactional(readOnly = true)
    public Page<SimpleCampaignResponse> searchCampaigns(String keyword, int page, int size, String approvalStatus) {
        log.info("캠페인 검색: keyword={}, page={}, size={}, approvalStatus={}", keyword, page, size, approvalStatus);

        if (approvalStatus != null && "만료됨".equalsIgnoreCase(approvalStatus)) {
            return searchExpiredCampaignsSimple(keyword, page, size);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Campaign> campaigns;

        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            try {
                String englishStatus = convertKoreanToEnglishStatus(approvalStatus);
                Campaign.ApprovalStatus status = Campaign.ApprovalStatus.valueOf(englishStatus.toUpperCase());
                campaigns = campaignRepository.findByKeywordAndApprovalStatus(keyword, status, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 승인 상태입니다: " + approvalStatus + ". 사용 가능한 값: 대기중, 승인됨, 거절됨, 만료됨");
            }
        } else {
            campaigns = campaignRepository.findByKeyword(keyword, pageable);
        }

        return campaigns.map(this::convertToSimpleCampaignResponse);
    }

    /**
     * 만료된 캠페인 검색 (간단한 버전)
     */
    private Page<SimpleCampaignResponse> searchExpiredCampaignsSimple(String keyword, int page, int size) {
        // 모든 캠페인을 가져와서 키워드로 필터링 후 만료된 것만 추가 필터링
        List<Campaign> allCampaigns = campaignRepository.findByKeywordAll(keyword);
        LocalDate currentDate = LocalDate.now();

        List<SimpleCampaignResponse> expiredCampaigns = allCampaigns.stream()
                .filter(campaign -> campaign.getRecruitmentEndDate() != null &&
                        campaign.getRecruitmentEndDate().isBefore(currentDate))
                .map(campaign -> {
                    SimpleCampaignResponse response = convertToSimpleCampaignResponse(campaign);
                    return SimpleCampaignResponse.builder()
                            .id(response.getId())
                            .title(response.getTitle())
                            .campaignType(response.getCampaignType())
                            .thumbnailUrl(response.getThumbnailUrl())
                            .productShortInfo(response.getProductShortInfo())
                            .maxApplicants(response.getMaxApplicants())
                            .recruitmentStartDate(response.getRecruitmentStartDate())
                            .recruitmentEndDate(response.getRecruitmentEndDate())
                            .selectionDate(response.getSelectionDate())
                            .approvalStatus("만료됨")
                            .approvalComment(response.getApprovalComment())
                            .approvalDate(response.getApprovalDate())
                            .build();
                })
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, expiredCampaigns.size());
        List<SimpleCampaignResponse> pagedExpiredCampaigns = start >= expiredCampaigns.size() ?
                List.of() : expiredCampaigns.subList(start, end);

        return new PageImpl<>(pagedExpiredCampaigns, PageRequest.of(page, size), expiredCampaigns.size());
    }

    /**
     * 캠페인 삭제 (관리자용)
     */
    public void deleteCampaign(String adminEmail, Long campaignId) {
        log.info("캠페인 삭제 처리: adminEmail={}, campaignId={}", adminEmail, campaignId);

        // 관리자 권한 확인
        User admin = userRepository.findByEmailAndRole(adminEmail,
                        com.example.adminservice.constant.UserRole.ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다"));

        if (!isAdmin(admin)) {
            throw new RuntimeException("관리자 권한이 필요합니다");
        }

        // 캠페인 조회
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("캠페인을 찾을 수 없습니다: " + campaignId));

        // 승인된 캠페인 삭제 시 추가 확인
        if (campaign.getApprovalStatus() == Campaign.ApprovalStatus.APPROVED) {
            log.warn("승인된 캠페인 삭제 시도: campaignId={}, adminEmail={}", campaignId, adminEmail);
            // 필요시 추가 검증 로직 (예: SUPER_ADMIN만 승인된 캠페인 삭제 가능)
        }

        // 캠페인 관련 KokPost 비활성화 처리
        try {
            kokPostService.deactivateKokPostsByCampaignId(campaignId);
            log.info("캠페인 관련 KokPost 비활성화 완료: campaignId={}", campaignId);
        } catch (Exception e) {
            log.error("캠페인 관련 KokPost 비활성화 실패: campaignId={}, error={}", 
                    campaignId, e.getMessage(), e);
            // 비활성화 실패해도 캠페인 삭제는 계속 진행
        }

        // 캠페인 삭제 전 알림 전송
        try {
            clientNotificationService.sendCampaignDeletionNotification(
                    campaign.getCreatorId(),
                    campaign.getId(),
                    campaign.getTitle(),
                    admin.getId()
            );
            log.info("캠페인 삭제 알림 전송 요청 완료: campaignId={}", campaign.getId());
        } catch (Exception e) {
            log.error("캠페인 삭제 알림 전송 실패: campaignId={}, error={}",
                    campaign.getId(), e.getMessage(), e);
        }

        // 캠페인 삭제
        campaignRepository.delete(campaign);
        log.info("캠페인 삭제 완료: campaignId={}", campaignId);
    }

    /**
     * 캠페인 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCampaignStats() {
        log.info("캠페인 통계 정보 조회");

        Map<String, Object> stats = new HashMap<>();

        long totalCampaigns = campaignRepository.count();
        stats.put("totalCampaigns", totalCampaigns);

        // 만료되지 않은 각 상태별 캠페인 수
        long pendingCount = campaignRepository.countByApprovalStatus(Campaign.ApprovalStatus.PENDING);
        long approvedCount = campaignRepository.countByApprovalStatus(Campaign.ApprovalStatus.APPROVED);
        long rejectedCount = campaignRepository.countByApprovalStatus(Campaign.ApprovalStatus.REJECTED);
        
        // 만료된 캠페인 수 (모든 상태에서 모집 마감일이 지난 캠페인)
        long expiredCount = calculateExpiredCampaignsCount();

        // 만료되지 않은 상태별 캠페인 수에서 만료된 것들을 제외
        long expiredPendingCount = campaignRepository.countExpiredCampaignsByStatus(Campaign.ApprovalStatus.PENDING);
        long expiredApprovedCount = campaignRepository.countExpiredCampaignsByStatus(Campaign.ApprovalStatus.APPROVED);
        long expiredRejectedCount = campaignRepository.countExpiredCampaignsByStatus(Campaign.ApprovalStatus.REJECTED);

        stats.put("pendingCampaigns", pendingCount - expiredPendingCount);
        stats.put("approvedCampaigns", approvedCount - expiredApprovedCount);
        stats.put("rejectedCampaigns", rejectedCount - expiredRejectedCount);
        stats.put("expiredCampaigns", expiredCount);

        // 로깅으로 검증
        log.debug("캠페인 통계: total={}, pending={} (만료제외 {}), approved={} (만료제외 {}), rejected={} (만료제외 {}), expired={}", 
                totalCampaigns, pendingCount, pendingCount - expiredPendingCount, 
                approvedCount, approvedCount - expiredApprovedCount, 
                rejectedCount, rejectedCount - expiredRejectedCount, expiredCount);

        return stats;
    }

    /**
     * 만료된 캠페인 개수 계산 (DB 쿼리로 직접 조회)
     */
    private long calculateExpiredCampaignsCount() {
        return campaignRepository.countExpiredCampaigns();
    }

    /**
     * 사용자가 관리자 권한을 가지고 있는지 확인
     */
    private boolean isAdmin(User user) {
        return user.getRole() != null &&
                ("ADMIN".equals(user.getRole().toString()) ||
                        "SUPER_ADMIN".equals(user.getRole().toString()));
    }

    /**
     * 배송 타입 캠페인인지 확인
     */
    private boolean isDeliveryType(String campaignType) {
        if (campaignType == null) {
            return false;
        }
        return campaignType.toLowerCase().contains("배송") ||
                campaignType.toLowerCase().contains("delivery") ||
                campaignType.toLowerCase().contains("택배") ||
                campaignType.toLowerCase().contains("온라인");
    }

    /**
     * Campaign 엔티티를 SimpleCampaignResponse DTO로 변환
     */
    private SimpleCampaignResponse convertToSimpleCampaignResponse(Campaign campaign) {
        return SimpleCampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .campaignType(campaign.getCampaignType())
                .thumbnailUrl(campaign.getThumbnailUrl())
                .productShortInfo(campaign.getProductShortInfo())
                .maxApplicants(campaign.getMaxApplicants())
                .recruitmentStartDate(campaign.getRecruitmentStartDate())
                .recruitmentEndDate(campaign.getRecruitmentEndDate())
                .selectionDate(campaign.getSelectionDate())
                .approvalStatus(getApprovalStatusInKorean(campaign.getApprovalStatus())) // 한글 변환
                .approvalComment(campaign.getApprovalComment())
                .approvalDate(campaign.getApprovalDateAsLocalDateTime()) // 호환성 메서드 사용
                .build();
    }

    /**
     * Campaign 엔티티를 PendingCampaignResponse DTO로 변환 (새로운 구조 대응)
     */
    private PendingCampaignResponse convertToPendingCampaignResponse(Campaign campaign) {
        PendingCampaignResponse.CreatorInfo creatorInfo = null;

        // 생성자 정보 조회 (새로운 구조: 객체 관계 사용)
        if (campaign.getCreator() != null) {
            User creator = campaign.getCreator();
            creatorInfo = PendingCampaignResponse.CreatorInfo.builder()
                    .id(creator.getId())
                    .nickname(creator.getNickname())
                    .email(creator.getEmail())
                    .accountType(creator.getAccountType() != null ?
                            creator.getAccountType().toString() : null)
                    .role(creator.getRole() != null ? creator.getRole().toString() : null)
                    .build();
        }

        // 회사 정보 조회 (새로운 구조: 객체 관계 사용)
        PendingCampaignResponse.CompanyInfo companyInfo = null;
        if (campaign.getCompany() != null) {
            Company company = campaign.getCompany();
            companyInfo = PendingCampaignResponse.CompanyInfo.builder()
                    .id(company.getId())
                    .companyName(company.getCompanyName())
                    .businessRegistrationNumber(company.getBusinessRegistrationNumber())
                    .contactPerson(company.getContactPerson())
                    .phoneNumber(company.getPhoneNumber())
                    .build();
        }

        // 위치 정보 조회 (새로운 구조: 1:1 관계 사용)
        PendingCampaignResponse.LocationInfo locationInfo = null;
        if (campaign.getLocation() != null && !isDeliveryType(campaign.getCampaignType())) {
            CampaignLocation location = campaign.getLocation();
            locationInfo = PendingCampaignResponse.LocationInfo.builder()
                    .id(location.getId())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .businessAddress(location.getBusinessAddress())
                    .businessDetailAddress(location.getBusinessDetailAddress())
                    .homepage(location.getHomepage())
                    .contactPhone(location.getContactPhone())
                    .visitAndReservationInfo(location.getVisitAndReservationInfo())
                    .hasCoordinates(location.hasCoordinates())
                    .build();
        }

        // 승인자 정보 조회 (승인된 경우에만)
        PendingCampaignResponse.ApproverInfo approverInfo = null;
        if (campaign.getApprovedBy() != null) {
            User approver = campaign.getApprovedBy();
            log.debug("승인자 정보 조회: campaignId={}, approverId={}, approverNickname={}",
                    campaign.getId(), approver.getId(), approver.getNickname());
            approverInfo = PendingCampaignResponse.ApproverInfo.builder()
                    .id(approver.getId())
                    .nickname(approver.getNickname())
                    .email(approver.getEmail())
                    .build();
        } else {
            log.debug("승인자 정보 없음: campaignId={}, approvalStatus={}",
                    campaign.getId(), campaign.getApprovalStatus());
        }

        // 미션 정보 조회 (새로운 구조: 1:1 관계 사용)
        PendingCampaignResponse.MissionInfo missionInfo = null;
        if (campaign.getMissionInfo() != null) {
            com.example.adminservice.domain.CampaignMissionInfo mission = campaign.getMissionInfo();
            missionInfo = PendingCampaignResponse.MissionInfo.builder()
                    .id(mission.getId())
                    .titleKeywords(mission.getTitleKeywords())
                    .bodyKeywords(mission.getBodyKeywords())
                    .numberOfVideo(mission.getNumberOfVideo())
                    .numberOfImage(mission.getNumberOfImage())
                    .numberOfText(mission.getNumberOfText())
                    .isMap(mission.getIsMap())
                    .missionGuide(mission.getMissionGuide())
                    .missionStartDate(mission.getMissionStartDate())
                    .missionDeadlineDate(mission.getMissionDeadlineDate())
                    .createdAt(mission.getCreatedAt().toLocalDateTime())
                    .updatedAt(mission.getUpdatedAt().toLocalDateTime())
                    .build();
        }

        // 카테고리 정보 조회
        PendingCampaignResponse.CategoryInfo categoryInfo = null;
        if (campaign.getCategory() != null) {
            CampaignCategory category = campaign.getCategory();
            log.debug("카테고리 정보 로드: campaignId={}, categoryId={}, type={}, name={}",
                    campaign.getId(), category.getId(), category.getType(), category.getName());
            categoryInfo = PendingCampaignResponse.CategoryInfo.builder()
                    .type(category.getType())
                    .name(category.getName())
                    .build();
        } else {
            log.warn("카테고리 정보가 없습니다: campaignId={}", campaign.getId());
        }

        return PendingCampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .campaignType(campaign.getCampaignType())
                .thumbnailUrl(campaign.getThumbnailUrl())
                .productShortInfo(campaign.getProductShortInfo())
                .maxApplicants(campaign.getMaxApplicants())
                .recruitmentStartDate(campaign.getRecruitmentStartDate())
                .recruitmentEndDate(campaign.getRecruitmentEndDate())
                .selectionDate(campaign.getSelectionDate())
                .reviewDeadlineDate(campaign.getReviewDeadlineDate()) // 호환성 메서드 사용
                .approvalStatus(getApprovalStatusInKorean(campaign.getApprovalStatus())) // 한글 변환
                .approvalComment(campaign.getApprovalComment())
                .approvalDate(campaign.getApprovalDateAsLocalDateTime()) // 호환성 메서드 사용
                .createdAt(campaign.getCreatedAtAsLocalDateTime()) // 호환성 메서드 사용
                .approver(approverInfo)
                .creator(creatorInfo)
                .company(companyInfo)
                .location(locationInfo)
                .missionInfo(missionInfo)
                .category(categoryInfo)
                .build();
    }

    /**
     * Campaign 엔티티를 CampaignApprovalResponse DTO로 변환 (새로운 구조 대응)
     */
    private CampaignApprovalResponse convertToCampaignApprovalResponse(Campaign campaign, User approver) {
        return CampaignApprovalResponse.builder()
                .campaignId(campaign.getId())
                .title(campaign.getTitle())
                .approvalStatus(getApprovalStatusInKorean(campaign.getApprovalStatus())) // 한글 변환
                .approvalComment(campaign.getApprovalComment())
                .approvalDate(campaign.getApprovalDateAsLocalDateTime()) // 호환성 메서드 사용
                .approvedBy(campaign.getApprovedById()) // 호환성 메서드 사용
                .approverName(approver != null ? approver.getNickname() : null)
                .build();
    }

    /**
     * 승인 상태를 한글로 변환
     */
    private String getApprovalStatusInKorean(Campaign.ApprovalStatus approvalStatus) {
        if (approvalStatus == null) {
            return "알 수 없음";
        }
        return approvalStatus.getDescription();
    }

    /**
     * 한글 승인 상태를 영문으로 변환
     */
    private String convertKoreanToEnglishStatus(String koreanStatus) {
        switch (koreanStatus) {
            case "대기중":
                return "PENDING";
            case "승인됨":
                return "APPROVED";
            case "거절됨":
                return "REJECTED";
            case "만료됨":
                return "EXPIRED";
            default:
                // 영문으로 입력된 경우 그대로 반환
                return koreanStatus;
        }
    }
}
