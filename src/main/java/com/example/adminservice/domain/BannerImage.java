package com.example.adminservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 배너 이미지 정보를 저장하는 엔티티 클래스
 * 
 * 메인 페이지나 특정 페이지에 표시되는 배너 이미지와
 * 클릭 시 이동할 URL, 제목, 설명, 포지션 정보를 관리합니다.
 */
@Entity
@Table(name = "banner_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BannerImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 배너 고유 식별자

    @Column(name = "title", nullable = false, length = 100)
    @Builder.Default
    private String title = "";  // 배너 제목

    @Column(name = "description", columnDefinition = "TEXT")
    @Builder.Default
    private String description = "";  // 배너 설명

    @Column(name = "banner_url", nullable = false, length = 1000)
    private String bannerUrl;  // 배너 이미지 URL

    @Column(name = "redirect_url", length = 1000)
    private String redirectUrl;  // 클릭 시 이동할 URL

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    @Builder.Default
    private Position position = Position.TOP;  // 배너 포지션

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;  // 배너 표시 순서 (낮을수록 상위)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 배너 생성 시간

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;  // 배너 정보 수정 시간

    /**
     * 배너 포지션 열거형
     */
    public enum Position {
        TOP("상단"),
        MIDDLE("중간"),
        BOTTOM("하단"),
        SIDEBAR("사이드바");

        private final String description;

        Position(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 기본 생성자 (배너 URL과 리다이렉트 URL만으로 생성)
     */
    public BannerImage(String bannerUrl, String redirectUrl) {
        this.bannerUrl = bannerUrl;
        this.redirectUrl = redirectUrl;
        this.title = "";
        this.description = "";
        this.position = Position.TOP;
    }

    /**
     * 배너 정보가 업데이트될 때 호출되어 수정 시간을 현재 시간으로 업데이트합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 배너가 처음 생성될 때 호출되어 생성 시간과 수정 시간을 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        
        // 빈 값 처리
        if (this.title == null) {
            this.title = "";
        }
        if (this.description == null) {
            this.description = "";
        }
        if (this.position == null) {
            this.position = Position.TOP;
        }
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}
