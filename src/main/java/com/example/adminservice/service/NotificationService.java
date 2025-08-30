package com.example.adminservice.service;

import com.example.adminservice.constant.EntityType;
import com.example.adminservice.constant.NotificationType;
import com.example.adminservice.domain.Notification;
import com.example.adminservice.dto.NotificationRequest;
import com.example.adminservice.dto.NotificationResponse;
import com.example.adminservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * 새로운 알림을 생성합니다.
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("새로운 알림 생성: userId={}, type={}, title={}", 
                request.getUserId(), request.getNotificationType(), request.getTitle());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedEntityId(request.getRelatedEntityId())
                .relatedEntityType(request.getRelatedEntityType())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("알림 생성 완료: ID={}", savedNotification.getId());

        // 실시간 알림 전송
        NotificationResponse response = NotificationResponse.from(savedNotification);
        webSocketNotificationService.sendNotificationToUser(savedNotification.getUserId(), response);

        return response;
    }

    /**
     * 특정 사용자의 모든 알림을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("사용자 알림 목록 조회: userId={}", userId);

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 알림을 페이징으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        log.info("사용자 알림 페이징 조회: userId={}, page={}, size={}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 특정 사용자의 읽지 않은 알림을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("사용자 읽지 않은 알림 조회: userId={}", userId);

        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 읽지 않은 알림 개수를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(Long userId) {
        log.info("사용자 읽지 않은 알림 개수 조회: userId={}", userId);
        
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 특정 알림을 읽음 상태로 변경합니다.
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);

        int updatedRows = notificationRepository.markAsReadById(notificationId, userId, LocalDateTime.now());
        
        if (updatedRows == 0) {
            throw new RuntimeException("알림을 찾을 수 없거나 권한이 없습니다: " + notificationId);
        }

        log.info("알림 읽음 처리 완료: notificationId={}", notificationId);

        // 읽지 않은 알림 개수 업데이트
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        webSocketNotificationService.sendUnreadCountUpdate(userId, unreadCount);
    }

    /**
     * 특정 사용자의 모든 알림을 읽음 상태로 변경합니다.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("사용자 모든 알림 읽음 처리: userId={}", userId);

        int updatedRows = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        
        log.info("모든 알림 읽음 처리 완료: userId={}, 처리된 알림 수={}", userId, updatedRows);

        // 읽지 않은 알림 개수 업데이트
        webSocketNotificationService.sendUnreadCountUpdate(userId, 0L);
    }

    /**
     * 특정 알림을 삭제합니다.
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("알림 삭제: notificationId={}, userId={}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("알림 삭제 권한이 없습니다: " + notificationId);
        }

        notificationRepository.delete(notification);
        log.info("알림 삭제 완료: notificationId={}", notificationId);
    }

    /**
     * 시스템 알림을 생성합니다. (모든 사용자 또는 특정 그룹)
     */
    @Transactional
    public void createSystemNotification(String title, String message, List<Long> userIds) {
        log.info("시스템 알림 생성: title={}, 대상 사용자 수={}", title, userIds.size());

        List<Notification> notifications = userIds.stream()
                .map(userId -> Notification.builder()
                        .userId(userId)
                        .notificationType(NotificationType.SYSTEM_NOTICE)
                        .title(title)
                        .message(message)
                        .relatedEntityType(EntityType.SYSTEM)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
        log.info("시스템 알림 생성 완료: {} 건", notifications.size());

        // 실시간 알림 전송 (임시 비활성화)
        // notifications.forEach(notification -> {
        //     NotificationResponse response = NotificationResponse.from(notification);
        //     webSocketNotificationService.sendBroadcastNotification(response);
        // });
    }

    /**
     * 캠페인 관련 알림을 생성합니다.
     */
    @Transactional
    public void createCampaignNotification(Long userId, Long campaignId, NotificationType type, String title, String message) {
        log.info("캠페인 알림 생성: userId={}, campaignId={}, type={}", userId, campaignId, type);

        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(type)
                .title(title)
                .message(message)
                .relatedEntityId(campaignId)
                .relatedEntityType(EntityType.CAMPAIGN)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("캠페인 알림 생성 완료: ID={}", savedNotification.getId());

        // 실시간 알림 전송 (임시 비활성화)
        // NotificationResponse response = NotificationResponse.from(savedNotification);
        // webSocketNotificationService.sendNotificationToUser(savedNotification.getUserId(), response);
    }

    /**
     * 오래된 알림을 정리합니다. (정기 작업용)
     */
    @Transactional
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        log.info("오래된 알림 정리 시작: 기준일={}", cutoffDate);
        
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
        
        log.info("오래된 알림 정리 완료: 삭제된 알림 수={}", deletedCount);
    }
}
