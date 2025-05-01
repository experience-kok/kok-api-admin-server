package com.example.adminservice.controller;

import com.example.adminservice.dto.AdminLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 이 컨트롤러는 실제 로그인 처리를 하지 않고 Swagger 문서화 목적으로만 사용됩니다.
 * 실제 로그인 처리는 AdminJwtAuthenticationFilter에서 수행됩니다.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "관리자 인증 API", description = "관리자 로그인 및 인증 관련 API")
public class AdminLoginDocController {

    @Operation(summary = "관리자 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public void loginDoc(@RequestBody AdminLoginRequest loginRequest) {
        // 이 메서드는 실행되지 않습니다. Swagger 문서화 목적으로만 존재합니다.
        throw new UnsupportedOperationException("이 메서드는 Swagger 문서화 목적으로만 존재합니다.");
    }
    
    /**
     * 로그인 응답 스키마를 정의하기 위한 내부 클래스
     */
    @Schema(description = "로그인 응답")
    public static class LoginResponse {
        @Schema(description = "JWT 액세스 토큰")
        private String accessToken;
        
        @Schema(description = "JWT 리프레시 토큰")
        private String refreshToken;
        
        @Schema(description = "관리자 이메일")
        private String email;

        // Getter, Setter (Swagger가 리플렉션 사용)
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
