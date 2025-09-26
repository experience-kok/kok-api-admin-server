package com.example.adminservice.repository;

import com.example.adminservice.domain.Campaign;
import com.example.adminservice.domain.CampaignApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 캠페인 Repository
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * ID로 캠페인 조회 (모든 연관 엔티티 로드)
     */
    @EntityGraph(attributePaths = {"creator", "company", "approvedBy", "location", "missionInfo"})
    Optional<Campaign> findById(Long id);

    /**
     * 승인 상태별 캠페인 조회 (페이징) - 연관 엔티티 로드
     */
    @EntityGraph(attributePaths = {"creator", "company", "approvedBy", "location", "missionInfo"})
    Page<Campaign> findByApprovalStatus(Campaign.ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * 모든 캠페인 조회 (페이징) - 연관 엔티티 로드
     */
    @EntityGraph(attributePaths = {"creator", "company", "approvedBy", "location", "missionInfo"})
    Page<Campaign> findAll(Pageable pageable);

    /**
     * 승인 대기 중인 캠페인 개수 조회
     */
    long countByApprovalStatus(Campaign.ApprovalStatus approvalStatus);

    /**
     * 특정 생성자의 캠페인 목록 조회
     */
    Page<Campaign> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 승인 상태별 캠페인 개수 조회
     */
    @Query("SELECT c.approvalStatus, COUNT(c) FROM Campaign c GROUP BY c.approvalStatus")
    List<Object[]> countByApprovalStatusGrouped();

    /**
     * 최근 생성된 캠페인 조회 (상태별 필터링 가능)
     */
    @Query("SELECT c FROM Campaign c WHERE (:status IS NULL OR c.approvalStatus = :status) ORDER BY c.createdAt DESC")
    Page<Campaign> findRecentCampaigns(@Param("status") Campaign.ApprovalStatus status, Pageable pageable);

    /**
     * 특정 기간 내 생성된 캠페인 조회
     */
    @Query("SELECT c FROM Campaign c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate ORDER BY c.createdAt DESC")
    List<Campaign> findCampaignsCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                               @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 키워드로 캠페인 검색 (제목, 제품정보, 생성자 닉네임, 회사명에서 검색)
     */
    @Query("SELECT c FROM Campaign c " +
            "LEFT JOIN c.creator creator " +
            "LEFT JOIN c.company company " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productShortInfo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productDetails) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(creator.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(company.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY c.createdAt DESC")
    Page<Campaign> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 키워드와 승인 상태로 캠페인 검색
     */
    @Query("SELECT c FROM Campaign c " +
            "LEFT JOIN c.creator creator " +
            "LEFT JOIN c.company company " +
            "WHERE c.approvalStatus = :status " +
            "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productShortInfo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productDetails) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(creator.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(company.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.createdAt DESC")
    Page<Campaign> findByKeywordAndApprovalStatus(@Param("keyword") String keyword,
                                                  @Param("status") Campaign.ApprovalStatus status,
                                                  Pageable pageable);

    /**
     * 키워드로 모든 캠페인 검색 (만료된 캠페인 검색용)
     */
    @Query("SELECT c FROM Campaign c " +
            "LEFT JOIN c.creator creator " +
            "LEFT JOIN c.company company " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productShortInfo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.productDetails) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(creator.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(company.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY c.createdAt DESC")
    List<Campaign> findByKeywordAll(@Param("keyword") String keyword);

    /**
     * 특정 생성자의 캠페인 목록 조회 (회사 정보 포함)
     */
    @Query("SELECT c FROM Campaign c " +
           "LEFT JOIN FETCH c.company comp " +
           "LEFT JOIN FETCH c.approvedBy ab " +
           "WHERE c.creator.id = :creatorId " +
           "ORDER BY c.createdAt DESC")
    Page<Campaign> findByCreatorIdWithCompanyInfo(@Param("creatorId") Long creatorId, Pageable pageable);

    /**
     * 사용자별 캠페인 승인 상태별 카운트 조회
     */
    @Query("SELECT c.approvalStatus, COUNT(c) FROM Campaign c " +
           "WHERE c.creator.id = :creatorId " +
           "GROUP BY c.approvalStatus")
    Object[][] countByCreatorIdGroupByApprovalStatus(@Param("creatorId") Long creatorId);

    /**
     * 특정 생성자의 특정 상태 캠페인 조회
     */
    @Query("SELECT c FROM Campaign c " +
           "LEFT JOIN FETCH c.company comp " +
           "LEFT JOIN FETCH c.approvedBy ab " +
           "WHERE c.creator.id = :creatorId AND c.approvalStatus = :status " +
           "ORDER BY c.createdAt DESC")
    Page<Campaign> findByCreatorIdAndApprovalStatus(@Param("creatorId") Long creatorId, 
                                                   @Param("status") Campaign.ApprovalStatus status, 
                                                   Pageable pageable);

    /**
     * 캠페인별 현재 신청자 수 조회
     */
    @Query("SELECT ca.campaign.id, COUNT(ca) FROM CampaignApplication ca " +
           "WHERE ca.campaign.id IN :campaignIds " +
           "GROUP BY ca.campaign.id")
    Object[][] countApplicationsByCampaignIds(@Param("campaignIds") List<Long> campaignIds);

    /**
     * 만료된 캠페인 수 조회 (모집 마감일이 현재 날짜보다 이전인 캠페인)
     */
    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.recruitmentEndDate < CURRENT_DATE")
    long countExpiredCampaigns();

    /**
     * 특정 승인 상태의 만료된 캠페인 수 조회
     */
    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.approvalStatus = :status AND c.recruitmentEndDate < CURRENT_DATE")
    long countExpiredCampaignsByStatus(@Param("status") Campaign.ApprovalStatus status);
}
