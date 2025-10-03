package com.example.adminservice.service;

import com.example.adminservice.domain.Campaign;
import com.example.adminservice.dto.ShortCampaignResponse;
import com.example.adminservice.dto.SimpleCampaignResponse;
import com.example.adminservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 캠페인 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /**
     * 모든 캠페인 목록 조회 (ID와 제목만)
     *
     * @return 캠페인 목록 (ID, Title)
     */
    public List<ShortCampaignResponse> getAllCampaigns() {
        log.info("모든 캠페인 목록 조회 시작");
        
        List<Campaign> campaigns = campaignRepository.findAll();
        
        List<ShortCampaignResponse> response = campaigns.stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        
        log.info("캠페인 목록 조회 완료: {} 건", response.size());
        return response;
    }

    /**
     * Campaign 엔티티를 SimpleCampaignResponse로 변환
     *
     * @param campaign 캠페인 엔티티
     * @return SimpleCampaignResponse DTO
     */
    private ShortCampaignResponse convertToSimpleResponse(Campaign campaign) {
        return ShortCampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .build();
    }
}
