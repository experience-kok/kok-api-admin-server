package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "관리자 로그인 요청")
public class AdminLoginRequest {
    @Schema(
        description = "관리자 이메일", 
        example = "admin1234@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    
    @Schema(
        description = "관리자 비밀번호", 
        example = "1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
