package com.example.adminservice.repository;

import com.example.adminservice.constant.NotificationType;
import com.example.adminservice.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 엔티티에 대한 데이터 접근 레포지토리
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 모든 알림을 최신순으로 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 특정 사용자의 알림을 페이징으로 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 타입 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.notificationType = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndNotificationType(@Param("userId") Long userId, 
                                                       @Param("type") NotificationType type);

    /**
     * 특정 사용자의 모든 알림을 읽음 상태로 변경
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * 특정 알림을 읽음 상태로 변경
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId AND n.userId = :userId")
    int markAsReadById(@Param("notificationId") Long notificationId, 
                      @Param("userId") Long userId, 
                      @Param("readAt") LocalDateTime readAt);

    /**
     * 특정 기간 이전의 알림 삭제 (정리 작업용)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 관련 엔티티와 연결된 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityId = :entityId AND n.relatedEntityType = :entityType ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedEntity(@Param("entityId") Long entityId, 
                                          @Param("entityType") String entityType);

    /**
     * 최근 N개의 알림 조회 (관리자용)
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotifications(Pageable pageable);

    /**
     * 특정 날짜 범위의 알림 통계
     */
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY n.notificationType")
    List<Object[]> getNotificationStatsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}
