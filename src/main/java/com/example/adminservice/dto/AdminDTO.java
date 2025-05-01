package com.example.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private Long id;
    private String username; // 이메일을 사용자명으로 사용
    private String name;     // 닉네임
    private String email;
    private LocalDateTime lastLoginAt; // updatedAt
    private LocalDateTime createdAt;
}
