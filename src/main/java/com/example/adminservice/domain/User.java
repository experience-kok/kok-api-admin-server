package com.example.adminservice.domain;

import com.example.adminservice.constant.AccountType;
import com.example.adminservice.constant.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider", length = 20, nullable = false)
    private String provider;
    
    @Column(name = "social_id", length = 50, nullable = false, unique = true)
    private String socialId;
    
    @Column(name = "email", length = 255, unique = true)
    private String email;
    
    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;
    
    @Column(name = "profile_img", length = 255)
    private String profileImg;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "gender", length = 10)
    @Builder.Default
    private String gender = "unknown";
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "password", length = 100)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20, nullable = false)
    @Builder.Default
    private AccountType accountType = AccountType.SOCIAL;
    
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
