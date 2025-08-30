package com.example.adminservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDTO {
    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String provider;
    private String accountType;
    private Boolean active;
    private Boolean emailVerified;
    private String gender;
    private Integer age;
    private String phone;
    private String profileImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
