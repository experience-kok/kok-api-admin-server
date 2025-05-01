package com.example.adminservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @Column(name = "provider")
    private String provider;
    
    @Column(name = "social_id", unique = true)
    private String socialId;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "profile_img")
    private String profileImg;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "account_type")
    private String accountType;
    
    @Column(name = "email_verified")
    private Boolean emailVerified;
    
    @Column(name = "active")
    private Boolean active;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
