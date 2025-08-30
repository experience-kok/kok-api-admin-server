package com.example.adminservice.dto;

import com.example.adminservice.constant.EntityType;
import com.example.adminservice.constant.NotificationType;
import com.example.adminservice.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 정보 응답")
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "수신자 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "알림 타입", example = "CAMPAIGN_APPROVED")
    private NotificationType notificationType;

    @Schema(description = "알림 제목", example = "캠페인이 승인되었습니다")
    private String title;

    @Schema(description = "알림 내용", example = "귀하의 캠페인 '여름 프로모션'이 승인되어 진행 가능합니다.")
    private String message;

    @Schema(description = "관련 엔티티 ID", example = "123")
    private Long relatedEntityId;

    @Schema(description = "관련 엔티티 타입", example = "CAMPAIGN")
    private EntityType relatedEntityType;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "생성 시간", example = "2024-07-31T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "읽은 시간", example = "2024-07-31T16:00:00")
    private LocalDateTime readAt;

    /**
     * Entity에서 DTO로 변환하는 정적 메서드
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
