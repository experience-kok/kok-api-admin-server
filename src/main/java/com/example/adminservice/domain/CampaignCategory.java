package com.example.adminservice.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 캠페인 카테고리 엔티티
 */
@Entity
@Table(name = "campaign_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_type", nullable = false, length = 50)
    private String type;

    @Column(name = "category_name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private java.time.ZonedDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private java.time.ZonedDateTime updatedAt;
}
