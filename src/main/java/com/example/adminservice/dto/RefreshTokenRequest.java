package com.example.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh 토큰은 필수입니다.")
    private String refreshToken;
}
