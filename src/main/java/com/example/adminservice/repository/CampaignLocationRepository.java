package com.example.adminservice.repository;

import com.example.adminservice.domain.CampaignLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 캠페인 위치 Repository
 */
@Repository
public interface CampaignLocationRepository extends JpaRepository<CampaignLocation, Long> {

    /**
     * 캠페인 ID로 위치 정보 조회
     */
    Optional<CampaignLocation> findByCampaignId(Long campaignId);

    /**
     * 캠페인 ID로 위치 정보 존재 여부 확인
     */
    boolean existsByCampaignId(Long campaignId);

    /**
     * 캠페인 ID로 위치 정보 삭제
     */
    void deleteByCampaignId(Long campaignId);
}
