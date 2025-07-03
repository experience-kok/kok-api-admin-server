package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.AdminLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이 컨트롤러는 실제 로그인 처리를 하지 않고 Swagger 문서화 목적으로만 사용됩니다.
 * 실제 로그인 처리는 AdminJwtAuthenticationFilter에서 수행됩니다.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "관리자 인증 API", description = "🔐 관리자 로그인, 토큰 갱신, 현재 사용자 정보 조회 등 인증 관련 API")
public class AdminLoginDocController {

    @Operation(
        summary = "관리자 로그인", 
        description = """
            이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
            
            ### 기본 관리자 계정
            - **이메일**: admin1234@example.com
            - **비밀번호**: 1234
            
            ### 응답 정보
            - **accessToken**: API 요청에 사용할 JWT 토큰 (1시간 유효)
            - **refreshToken**: 액세스 토큰 갱신용 토큰 (7일 유효)
            - **email**: 로그인한 관리자 이메일
            
            ### 사용법
            1. 로그인 후 받은 accessToken을 복사
            2. 우측 상단 'Authorize' 버튼 클릭
            3. Bearer {accessToken} 형식으로 입력 (Bearer는 자동 추가됨)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ 로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginSuccessResponse.class),
                examples = @ExampleObject(
                    name = "로그인 성공 응답",
                    summary = "정상적인 로그인 응답",
                    value = """
                        {
                          "success": true,
                          "message": "로그인 성공",
                          "status": 200,
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDQ2NzIwMDAsImV4cCI6MTcwNDY3NTYwMCwicm9sZSI6IkFETUlOIn0.example-jwt-token",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDQ2NzIwMDAsImV4cCI6MTcwNTI3NjgwMH0.example-refresh-token",
                            "email": "admin1234@example.com"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.Error.class),
                examples = @ExampleObject(
                    name = "로그인 실패 응답",
                    summary = "잘못된 이메일 또는 비밀번호",
                    value = """
                        {
                          "success": false,
                          "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
                          "errorCode": "AUTHENTICATION_FAILED",
                          "status": 401
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/login")
    public void loginDoc(@RequestBody AdminLoginRequest loginRequest) {
        // 이 메서드는 실행되지 않습니다. Swagger 문서화 목적으로만 존재합니다.
        throw new UnsupportedOperationException("이 메서드는 Swagger 문서화 목적으로만 존재합니다.");
    }
    
    /**
     * 로그인 성공 응답 스키마
     */
    @Schema(description = "로그인 성공 응답")
    public static class LoginSuccessResponse {
        @Schema(description = "성공 여부", example = "true")
        private boolean success = true;
        
        @Schema(description = "응답 메시지", example = "로그인 성공")
        private String message;
        
        @Schema(description = "HTTP 상태 코드", example = "200")
        private int status;
        
        @Schema(description = "토큰 데이터")
        private TokenData data;
        
        @Schema(description = "토큰 정보")
        public static class TokenData {
            @Schema(
                description = "JWT 액세스 토큰 (1시간 유효)", 
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDQ2NzIwMDAsImV4cCI6MTcwNDY3NTYwMCwicm9sZSI6IkFETUlOIn0.example-jwt-token"
            )
            private String accessToken;
            
            @Schema(
                description = "JWT 리프레시 토큰 (7일 유효)", 
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDQ2NzIwMDAsImV4cCI6MTcwNTI3NjgwMH0.example-refresh-token"
            )
            private String refreshToken;
            
            @Schema(description = "로그인한 관리자 이메일", example = "admin1234@example.com")
            private String email;

            // Getters and Setters
            public String getAccessToken() { return accessToken; }
            public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
            public String getRefreshToken() { return refreshToken; }
            public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public TokenData getData() { return data; }
        public void setData(TokenData data) { this.data = data; }
    }
}
