package com.example.adminservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 캠페인 정보를 저장하는 엔티티 클래스 (새로운 구조 적용)
 */
@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "campaign_type", nullable = false, length = 50)
    private String campaignType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "product_short_info", length = 50, nullable = false)
    private String productShortInfo;

    @Column(name = "max_applicants")
    private Integer maxApplicants;  // 최대 신청 가능 인원 수

    @Column(name = "product_details", nullable = false, columnDefinition = "TEXT")
    private String productDetails;  // 제공되는 제품/서비스에 대한 상세 정보

    // 날짜 필드들 - 논리적 순서에 따라 정렬
    @Column(name = "recruitment_start_date", nullable = false)
    private LocalDate recruitmentStartDate;  // 모집 시작 날짜

    @Column(name = "recruitment_end_date")
    private LocalDate recruitmentEndDate;  // 모집 종료 날짜 (상시 캠페인일 경우 null)

    @Column(name = "selection_date")
    private LocalDate selectionDate;  // 참여자 선정 날짜 (상시 캠페인일 경우 null)

    @Column(name = "selection_criteria", columnDefinition = "TEXT")
    private String selectionCriteria;  // 선정 기준

    @Column(name = "is_always_open", nullable = false)
    @Builder.Default
    private Boolean isAlwaysOpen = false;  // 상시 등록 여부 (상시 캠페인은 방문형만 가능)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CampaignCategory category;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CampaignApplication> applications = new ArrayList<>();

    @OneToOne(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private CampaignLocation location;

    @OneToOne(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private CampaignMissionInfo missionInfo;

    // 승인 관련 필드들
    @Column(name = "approval_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    @Column(name = "approval_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();



    /**
     * 승인 상태 열거형
     */
    public enum ApprovalStatus {
        PENDING("대기중"),
        APPROVED("승인됨"),
        REJECTED("거절됨");

        private final String description;

        ApprovalStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // =========================
    // 호환성을 위한 메서드들
    // =========================

    /**
     * 기존 creatorId 호환성을 위한 메서드
     */
    @Transient
    public Long getCreatorId() {
        return creator != null ? creator.getId() : null;
    }

    /**
     * 기존 companyId 호환성을 위한 메서드
     */
    @Transient
    public Long getCompanyId() {
        return company != null ? company.getId() : null;
    }

    /**
     * 기존 categoryId 호환성을 위한 메서드
     */
    @Transient
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    /**
     * 기존 approvedBy ID 호환성을 위한 메서드
     */
    @Transient
    public Long getApprovedById() {
        return approvedBy != null ? approvedBy.getId() : null;
    }

    /**
     * 기존 reviewStartDate 호환성을 위한 메서드
     */
    @Transient
    public LocalDate getReviewStartDate() {
        return missionInfo != null ? missionInfo.getMissionStartDate() : null;
    }

    /**
     * 기존 reviewDeadlineDate 호환성을 위한 메서드
     */
    @Transient
    public LocalDate getReviewDeadlineDate() {
        return missionInfo != null ? missionInfo.getMissionDeadlineDate() : null;
    }

    /**
     * 기존 missionGuide 호환성을 위한 메서드
     */
    @Transient
    public String getMissionGuide() {
        return missionInfo != null ? missionInfo.getMissionGuide() : null;
    }

    /**
     * 기존 missionKeywords 호환성을 위한 메서드
     * (titleKeywords + bodyKeywords 합쳐서 반환)
     */
    @Transient
    public String[] getMissionKeywords() {
        if (missionInfo == null) {
            return new String[0];
        }

        String[] titleKeywords = missionInfo.getTitleKeywords();
        String[] bodyKeywords = missionInfo.getBodyKeywords();

        // 두 배열을 합치기
        List<String> allKeywords = new ArrayList<>();
        if (titleKeywords != null) {
            allKeywords.addAll(List.of(titleKeywords));
        }
        if (bodyKeywords != null) {
            allKeywords.addAll(List.of(bodyKeywords));
        }

        return allKeywords.toArray(new String[0]);
    }

    /**
     * LocalDateTime 호환성을 위한 메서드들
     */
    @Transient
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt != null ? createdAt.toLocalDateTime() : null;
    }

    @Transient
    public LocalDateTime getUpdatedAtAsLocalDateTime() {
        return updatedAt != null ? updatedAt.toLocalDateTime() : null;
    }

    @Transient
    public LocalDateTime getApprovalDateAsLocalDateTime() {
        return approvalDate != null ? approvalDate.toLocalDateTime() : null;
    }

    // =========================
    // 기존 비즈니스 메서드들
    // =========================

    /**
     * 캠페인 승인 처리 (User 객체 사용)
     */
    public void approve(User approver, String comment) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvalComment = comment;
        this.approvalDate = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 캠페인 승인 처리 (ID 사용 - 기존 호환성)
     */
    public void approve(Long adminUserId, String comment) {
        // ID만으로는 User 객체를 만들 수 없으므로,
        // Service에서 User 객체를 조회해서 approve(User, String) 메서드를 호출하도록 변경 필요
        throw new UnsupportedOperationException("User 객체를 사용하는 approve(User, String) 메서드를 사용해주세요");
    }

    /**
     * 캠페인 거절 처리 (User 객체 사용)
     */
    public void reject(User approver, String comment) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = approver;
        this.approvalComment = comment;
        this.approvalDate = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 캠페인 거절 처리 (ID 사용 - 기존 호환성)
     */
    public void reject(Long adminUserId, String comment) {
        // ID만으로는 User 객체를 만들 수 없으므로,
        // Service에서 User 객체를 조회해서 reject(User, String) 메서드를 호출하도록 변경 필요
        throw new UnsupportedOperationException("User 객체를 사용하는 reject(User, String) 메서드를 사용해주세요");
    }

    /**
     * 승인 상태를 대기로 변경
     */
    public void resetApprovalStatus() {
        this.approvalStatus = ApprovalStatus.PENDING;
        this.approvedBy = null;
        this.approvalComment = null;
        this.approvalDate = null;
        this.updatedAt = ZonedDateTime.now();
    }

    // =========================
    // 추가된 비즈니스 메서드들
    // =========================

    /**
     * 캠페인 미션 정보를 설정합니다.
     */
    public void setMissionInfo(CampaignMissionInfo missionInfo) {
        this.missionInfo = missionInfo;
        if (missionInfo != null) {
            missionInfo.setCampaign(this);
        }
    }

    /**
     * 미션 정보가 있는지 확인합니다.
     */
    @Transient
    public boolean hasMissionInfo() {
        return missionInfo != null;
    }

    /**
     * 캠페인 신청을 추가합니다.
     */
    public void addApplication(CampaignApplication application) {
        if (application == null) {
            return;
        }
        applications.add(application);
        application.setCampaign(this);
    }

    /**
     * 캠페인 신청을 제거합니다.
     */
    public void removeApplication(CampaignApplication application) {
        if (application == null) {
            return;
        }
        applications.remove(application);
        application.setCampaign(null);
    }

    /**
     * 캠페인 위치를 설정합니다.
     */
    public void setLocation(CampaignLocation location) {
        this.location = location;
        if (location != null) {
            location.setCampaign(this);
        }
    }

    /**
     * 위치 정보가 있는지 확인
     */
    @Transient
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * 현재 신청자 수를 반환합니다.
     */
    @Transient
    public int getCurrentApplicantCount() {
        return (int) applications.stream()
                .filter(app -> app.getApplicationStatus() == com.example.adminservice.constant.ApplicationStatus.APPLIED)
                .count();
    }

    /**
     * 상시 캠페인인지 확인합니다.
     */
    @Transient
    public boolean isAlwaysOpenCampaign() {
        return Boolean.TRUE.equals(isAlwaysOpen);
    }

    /**
     * 상시 캠페인 유효성을 검증합니다.
     * 상시 캠페인은 모집 시작일을 제외하고 다른 날짜 필드들이 null이어야 합니다.
     */
    @Transient
    public boolean isValidAlwaysOpenCampaign() {
        if (!isAlwaysOpenCampaign()) {
            return true; // 일반 캠페인은 별도 검증
        }
        
        return recruitmentStartDate != null &&
               recruitmentEndDate == null &&
               selectionDate == null;
    }

    /**
     * 일반 캠페인 유효성을 검증합니다.
     * 일반 캠페인은 모든 필수 날짜 필드가 설정되어야 합니다.
     */
    @Transient
    public boolean isValidRegularCampaign() {
        if (isAlwaysOpenCampaign()) {
            return true; // 상시 캠페인은 별도 검증
        }
        
        return recruitmentStartDate != null &&
               recruitmentEndDate != null &&
               selectionDate != null &&
               !recruitmentStartDate.isAfter(recruitmentEndDate) &&
               !recruitmentEndDate.isAfter(selectionDate);
    }

    // =========================
    // JPA 이벤트 메서드들
    // =========================

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = ZonedDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = ZonedDateTime.now();
        }
    }
}