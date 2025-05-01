package com.example.adminservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sns_platforms")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "user")
public class UserSnsPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "platform_type", nullable = false)
    private String platformType;

    @Column(name = "account_url", nullable = false)
    private String accountUrl;

    @Column(name = "follower_count")
    private Integer followerCount;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public UserSnsPlatform(User user, String platformType, String accountUrl, Integer followerCount) {
        this.user = user;
        this.platformType = platformType;
        this.accountUrl = accountUrl;
        this.followerCount = followerCount;
    }

    public void updateFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
        this.lastCrawledAt = LocalDateTime.now();
    }

    public void updateLastCrawledAt(LocalDateTime lastCrawledAt) {
        this.lastCrawledAt = lastCrawledAt;
    }
}