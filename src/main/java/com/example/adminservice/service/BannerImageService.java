package com.example.adminservice.service;

import com.example.adminservice.domain.BannerImage;
import com.example.adminservice.dto.BannerImageRequest;
import com.example.adminservice.dto.BannerImageResponse;
import com.example.adminservice.repository.BannerImageRepository;
import com.example.adminservice.util.S3UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 배너 이미지 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerImageService {

    private final BannerImageRepository bannerImageRepository;
    private final S3Service s3Service;

    /**
     * 새로운 배너 이미지를 생성합니다.
     *
     * @param request 배너 이미지 생성 요청
     * @return 생성된 배너 이미지 정보
     */
    @Transactional
    public BannerImageResponse createBanner(BannerImageRequest request) {
        log.info("새로운 배너 이미지 생성: {}", request.getBannerUrl());

        // Presigned URL에서 쿼리 파라미터 제거
        String cleanBannerUrl = S3UrlUtils.cleanS3Url(request.getBannerUrl());
        log.info("URL 정리 완료: {} -> {}", request.getBannerUrl(), cleanBannerUrl);

        // 16:9 비율로 이미지 처리
        String processedBannerUrl = s3Service.processBannerImageTo16x9(cleanBannerUrl);

        // 새 필드들 처리 (없으면 빈 문자열로 설정)
        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty()) 
                        ? request.getTitle().trim() : "";
        String description = (request.getDescription() != null && !request.getDescription().trim().isEmpty()) 
                            ? request.getDescription().trim() : "";
        String redirectUrl = (request.getRedirectUrl() != null && !request.getRedirectUrl().trim().isEmpty()) 
                            ? request.getRedirectUrl().trim() : "";
        
        // Position 처리 (없으면 TOP으로 설정)
        BannerImage.Position position = BannerImage.Position.TOP;
        if (request.getPosition() != null && !request.getPosition().trim().isEmpty()) {
            try {
                position = BannerImage.Position.valueOf(request.getPosition().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 포지션 값 '{}', TOP으로 설정함", request.getPosition());
            }
        }

        // 표시 순서 처리 (없으면 최대값 + 1로 설정)
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = bannerImageRepository.findMaxDisplayOrder() + 1;
        }

        BannerImage bannerImage = BannerImage.builder()
                .title(title)
                .description(description)
                .bannerUrl(processedBannerUrl)
                .redirectUrl(redirectUrl)
                .position(position)
                .displayOrder(displayOrder)
                .build();

        BannerImage savedBanner = bannerImageRepository.save(bannerImage);
        log.info("배너 이미지 생성 완료: ID={}, title={}, position={}", 
                savedBanner.getId(), savedBanner.getTitle(), savedBanner.getPosition());

        return BannerImageResponse.from(savedBanner);
    }

    /**
     * 배너 이미지를 수정합니다.
     *
     * @param id 배너 ID
     * @param request 배너 이미지 수정 요청
     * @return 수정된 배너 이미지 정보
     */
    @Transactional
    public BannerImageResponse updateBanner(Long id, BannerImageRequest request) {
        log.info("배너 이미지 수정: ID={}", id);

        BannerImage bannerImage = bannerImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다: " + id));

        // Presigned URL에서 쿼리 파라미터 제거
        String cleanBannerUrl = S3UrlUtils.cleanS3Url(request.getBannerUrl());
        log.info("URL 정리 완료: {} -> {}", request.getBannerUrl(), cleanBannerUrl);

        // 새로운 이미지 URL이 제공된 경우에만 16:9 처리
        String processedBannerUrl = cleanBannerUrl;
        if (!cleanBannerUrl.equals(bannerImage.getBannerUrl())) {
            processedBannerUrl = s3Service.processBannerImageTo16x9(cleanBannerUrl);
        }

        // 새 필드들 처리 (없으면 빈 문자열로 설정)
        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty()) 
                        ? request.getTitle().trim() : "";
        String description = (request.getDescription() != null && !request.getDescription().trim().isEmpty()) 
                            ? request.getDescription().trim() : "";
        String redirectUrl = (request.getRedirectUrl() != null && !request.getRedirectUrl().trim().isEmpty()) 
                            ? request.getRedirectUrl().trim() : "";
        
        // Position 처리 (없으면 TOP으로 설정)
        BannerImage.Position position = BannerImage.Position.TOP;
        if (request.getPosition() != null && !request.getPosition().trim().isEmpty()) {
            try {
                position = BannerImage.Position.valueOf(request.getPosition().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 포지션 값 '{}', TOP으로 설정함", request.getPosition());
            }
        }

        // 표시 순서 처리 (없으면 최대값 + 1로 설정)
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = bannerImageRepository.findMaxDisplayOrder() + 1;
        }

        // 필드 업데이트
        bannerImage.setTitle(title);
        bannerImage.setDescription(description);
        bannerImage.setBannerUrl(processedBannerUrl);
        bannerImage.setRedirectUrl(redirectUrl);
        bannerImage.setPosition(position);
        bannerImage.setDisplayOrder(displayOrder);

        BannerImage updatedBanner = bannerImageRepository.save(bannerImage);
        log.info("배너 이미지 수정 완료: ID={}, title={}, position={}, displayOrder={}", 
                updatedBanner.getId(), updatedBanner.getTitle(), updatedBanner.getPosition(), updatedBanner.getDisplayOrder());

        return BannerImageResponse.from(updatedBanner);
    }

    /**
     * 배너 이미지를 삭제합니다.
     *
     * @param id 배너 ID
     */
    @Transactional
    public void deleteBanner(Long id) {
        log.info("배너 이미지 삭제: ID={}", id);

        BannerImage bannerImage = bannerImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다: " + id));

        bannerImageRepository.delete(bannerImage);
        log.info("배너 이미지 삭제 완료: ID={}", id);
    }

    /**
     * 특정 배너 이미지를 조회합니다.
     *
     * @param id 배너 ID
     * @return 배너 이미지 정보
     */
    @Transactional(readOnly = true)
    public BannerImageResponse getBanner(Long id) {
        log.info("배너 이미지 조회: ID={}", id);

        BannerImage bannerImage = bannerImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다: " + id));

        return BannerImageResponse.from(bannerImage);
    }

    /**
     * 모든 배너 이미지 목록을 조회합니다.
     *
     * @return 배너 이미지 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<BannerImageResponse> getAllBanners() {
        log.info("모든 배너 이미지 목록 조회");

        return bannerImageRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(BannerImageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 배너 이미지 목록을 ID 순으로 조회합니다.
     *
     * @return 배너 이미지 목록 (ID 순)
     */
    @Transactional(readOnly = true)
    public List<BannerImageResponse> getAllBannersOrderById() {
        log.info("모든 배너 이미지 목록 조회 (ID 순)");

        return bannerImageRepository.findAllOrderByIdAsc()
                .stream()
                .map(BannerImageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 배너 이미지 목록을 표시 순서대로 조회합니다.
     *
     * @return 배너 이미지 목록 (표시 순서순)
     */
    @Transactional(readOnly = true)
    public List<BannerImageResponse> getAllBannersOrderByDisplayOrder() {
        log.info("모든 배너 이미지 목록 조회 (표시 순서순)");

        return bannerImageRepository.findAllOrderByDisplayOrder()
                .stream()
                .map(BannerImageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 배너들의 표시 순서를 일괄 업데이트합니다.
     *
     * @param bannerOrders 순서 변경할 배너 목록
     * @return 업데이트된 배너 목록
     */
    @Transactional
    public List<BannerImageResponse> updateBannerOrders(List<com.example.adminservice.dto.BannerOrderUpdateRequest.BannerOrderItem> bannerOrders) {
        log.info("배너 순서 일괄 업데이트: {} 개 배너", bannerOrders.size());

        List<BannerImage> updatedBanners = new java.util.ArrayList<>();

        for (com.example.adminservice.dto.BannerOrderUpdateRequest.BannerOrderItem orderItem : bannerOrders) {
            BannerImage banner = bannerImageRepository.findById(orderItem.getId())
                    .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다: " + orderItem.getId()));

            banner.setDisplayOrder(orderItem.getDisplayOrder());
            BannerImage savedBanner = bannerImageRepository.save(banner);
            updatedBanners.add(savedBanner);

            log.debug("배너 순서 업데이트: ID={}, 새 순서={}", orderItem.getId(), orderItem.getDisplayOrder());
        }

        log.info("배너 순서 일괄 업데이트 완료");

        return updatedBanners.stream()
                .map(BannerImageResponse::from)
                .collect(Collectors.toList());
    }
}
