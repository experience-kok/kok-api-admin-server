package com.example.adminservice.service;

import com.example.adminservice.dto.notification.CampaignStatusNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 클라이언트 프로젝트의 알림 API를 호출하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    @Qualifier("clientApiWebClient")
    private final WebClient clientApiWebClient;

    @Value("${client.api.notification.timeout:10}")
    private int notificationTimeoutSeconds;

    /**
     * 캠페인 승인 알림을 클라이언트 프로젝트로 전송
     */
    public void sendCampaignApprovalNotification(Long userId, Long campaignId, String campaignTitle, Long adminId) {
        log.info("캠페인 승인 알림 전송: userId={}, campaignId={}, campaignTitle={}", userId, campaignId, campaignTitle);

        CampaignStatusNotificationRequest request = CampaignStatusNotificationRequest.builder()
                .userId(userId)
                .campaignId(campaignId)
                .campaignTitle(campaignTitle)
                .approvalStatus("APPROVED")
                .adminComment(null)
                .adminId(adminId)
                .build();

        sendNotificationAsync(request, "승인");
    }

    /**
     * 캠페인 거절 알림을 클라이언트 프로젝트로 전송
     */
    public void sendCampaignRejectionNotification(Long userId, Long campaignId, String campaignTitle,
                                                  String rejectionReason, Long adminId) {
        log.info("캠페인 거절 알림 전송: userId={}, campaignId={}, campaignTitle={}, reason={}",
                userId, campaignId, campaignTitle, rejectionReason);

        CampaignStatusNotificationRequest request = CampaignStatusNotificationRequest.builder()
                .userId(userId)
                .campaignId(campaignId)
                .campaignTitle(campaignTitle)
                .approvalStatus("REJECTED")
                .adminComment(rejectionReason)
                .adminId(adminId)
                .build();

        sendNotificationAsync(request, "거절");
    }

    /**
     * 캠페인 삭제 알림을 클라이언트 프로젝트로 전송
     */
    public void sendCampaignDeletionNotification(Long userId, Long campaignId, String campaignTitle, Long adminId) {
        log.info("캠페인 삭제 알림 전송: userId={}, campaignId={}, campaignTitle={}", userId, campaignId, campaignTitle);

        CampaignStatusNotificationRequest request = CampaignStatusNotificationRequest.builder()
                .userId(userId)
                .campaignId(campaignId)
                .campaignTitle(campaignTitle)
                .approvalStatus("DELETED")
                .adminComment("관리자에 의해 캠페인이 삭제되었습니다.")
                .adminId(adminId)
                .build();

        sendNotificationAsync(request, "삭제");
    }

    /**
     * 비동기로 클라이언트 프로젝트에 알림 전송
     */
    private void sendNotificationAsync(CampaignStatusNotificationRequest request, String actionType) {
        clientApiWebClient
                .post()
                .uri("/api/notifications/campaign-status")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(notificationTimeoutSeconds))
                .retry(2) // 2번 재시도
                .doOnSuccess(response -> {
                    log.info("캠페인 {} 알림 전송 성공: userId={}, campaignId={}, response={}",
                            actionType, request.getUserId(), request.getCampaignId(), response);
                })
                .doOnError(error -> {
                    log.error("캠페인 {} 알림 전송 실패: userId={}, campaignId={}, error={}",
                            actionType, request.getUserId(), request.getCampaignId(), error.getMessage());
                })
                .onErrorResume(error -> {
                    // 알림 전송 실패가 메인 로직에 영향을 주지 않도록 에러를 무시
                    log.warn("캠페인 {} 알림 전송 최종 실패했지만 계속 진행: userId={}, campaignId={}, 사유: {}",
                            actionType, request.getUserId(), request.getCampaignId(), error.getMessage());
                    return Mono.empty();
                })
                .subscribe(); // 비동기 실행
    }

    /**
     * 클라이언트 API 서버 연결 상태 확인
     */
    public boolean isClientApiServerAvailable() {
        try {
            String response = clientApiWebClient
                    .get()
                    .uri("/actuator/health")  // Spring Boot Actuator health 엔드포인트
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("클라이언트 API 서버 상태 확인 성공: {}", response);
            return true;

        } catch (Exception e) {
            log.warn("클라이언트 API 서버 연결 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 테스트용 알림 전송
     */
    public void sendTestNotification(Long userId, String message) {
        log.info("테스트 알림 전송: userId={}, message={}", userId, message);

        CampaignStatusNotificationRequest request = CampaignStatusNotificationRequest.builder()
                .userId(userId)
                .campaignId(999L)  // 테스트용 캠페인 ID
                .campaignTitle("테스트 알림: " + message)
                .approvalStatus("APPROVED")
                .adminComment("관리자 프로젝트에서 전송한 테스트 알림입니다.")
                .adminId(1L)
                .build();

        sendNotificationAsync(request, "테스트");
    }
}
