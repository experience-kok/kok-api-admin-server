package com.example.adminservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDetailResponse {
    private Long id;
    private String companyName;
    private String businessRegistrationNumber;
    private String contactPerson;
    private String phoneNumber;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 등록자 정보
    private UserSummaryDTO userInfo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDTO {
        private Long id;
        private String email;
        private String nickname;
        private String role;
        private Boolean active;
        private String phone;
        private String gender;
        private Integer age;
        private String provider;
        private Boolean emailVerified;
        private String profileImg;  // 프로필 이미지 추가
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
