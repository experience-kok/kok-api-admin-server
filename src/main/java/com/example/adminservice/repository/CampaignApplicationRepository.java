package com.example.adminservice.repository;

import com.example.adminservice.constant.ApplicationStatus;
import com.example.adminservice.domain.CampaignApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 캠페인 신청 Repository
 */
@Repository
public interface CampaignApplicationRepository extends JpaRepository<CampaignApplication, Long> {

    /**
     * 사용자의 캠페인 신청 내역을 페이징하여 조회 (캠페인 정보 포함)
     */
    @Query("SELECT ca FROM CampaignApplication ca " +
           "JOIN FETCH ca.campaign c " +
           "LEFT JOIN FETCH c.company comp " +
           "WHERE ca.user.id = :userId " +
           "ORDER BY ca.appliedAt DESC")
    Page<CampaignApplication> findByUserIdWithCampaignInfo(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 특정 상태 신청 내역 조회
     */
    @Query("SELECT ca FROM CampaignApplication ca " +
           "JOIN FETCH ca.campaign c " +
           "LEFT JOIN FETCH c.company comp " +
           "WHERE ca.user.id = :userId AND ca.applicationStatus = :status " +
           "ORDER BY ca.appliedAt DESC")
    Page<CampaignApplication> findByUserIdAndStatus(@Param("userId") Long userId, 
                                                    @Param("status") ApplicationStatus status, 
                                                    Pageable pageable);

    /**
     * 특정 캠페인의 신청자 목록을 페이징하여 조회 (사용자 정보 포함)
     */
    @Query("SELECT ca FROM CampaignApplication ca " +
           "JOIN FETCH ca.user u " +
           "WHERE ca.campaign.id = :campaignId " +
           "ORDER BY ca.appliedAt DESC")
    Page<CampaignApplication> findByCampaignIdWithUserInfo(@Param("campaignId") Long campaignId, Pageable pageable);

    /**
     * 특정 캠페인의 특정 상태 신청자 목록 조회
     */
    @Query("SELECT ca FROM CampaignApplication ca " +
           "JOIN FETCH ca.user u " +
           "WHERE ca.campaign.id = :campaignId AND ca.applicationStatus = :status " +
           "ORDER BY ca.appliedAt DESC")
    Page<CampaignApplication> findByCampaignIdAndStatus(@Param("campaignId") Long campaignId, 
                                                       @Param("status") ApplicationStatus status, 
                                                       Pageable pageable);

    /**
     * 특정 캠페인의 총 신청자 수 조회
     */
    long countByCampaignId(Long campaignId);

    /**
     * 특정 캠페인의 상태별 신청자 수 조회
     */
    long countByCampaignIdAndApplicationStatus(Long campaignId, ApplicationStatus status);
}
