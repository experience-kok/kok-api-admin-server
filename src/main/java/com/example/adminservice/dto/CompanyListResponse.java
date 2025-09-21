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
public class CompanyListResponse {
    private Long id;
    private String companyName;
    private String businessRegistrationNumber;
    private String contactPerson;
    private String phoneNumber;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
