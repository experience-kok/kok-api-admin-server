package com.example.adminservice.domain;

import com.example.adminservice.constant.EntityType;
import com.example.adminservice.constant.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 알림 정보를 저장하는 엔티티 클래스
 * 
 * 실시간 알림 서비스를 위한 알림 데이터를 관리합니다.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_notifications_user_unread", columnList = "user_id, is_read, created_at"),
    @Index(name = "idx_notifications_type", columnList = "notification_type"),
    @Index(name = "idx_notifications_related_entity", columnList = "related_entity_id, related_entity_type"),
    @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 알림 고유 ID

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 알림 수신자 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;  // 알림 타입

    @Column(name = "title", nullable = false, length = 200)
    private String title;  // 알림 제목

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;  // 알림 내용

    @Column(name = "related_entity_id")
    private Long relatedEntityId;  // 관련 엔티티 ID (캠페인 ID, 신청 ID 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type", length = 50)
    private EntityType relatedEntityType;  // 관련 엔티티 타입

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;  // 읽음 여부

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 알림 생성 시간

    @Column(name = "read_at")
    private LocalDateTime readAt;  // 읽은 시간

    // 외래키 참조 (User 엔티티와 연관관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 알림을 읽지 않음 상태로 변경
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * 생성자 (필수 필드만)
     */
    public Notification(Long userId, NotificationType notificationType, String title, String message) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.isRead = false;
    }

    /**
     * 생성자 (관련 엔티티 정보 포함)
     */
    public Notification(Long userId, NotificationType notificationType, String title, String message, 
                       Long relatedEntityId, EntityType relatedEntityType) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.isRead = false;
    }
}
