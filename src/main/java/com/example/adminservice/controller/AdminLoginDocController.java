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
@Tag(name = "관리자 인증 API", description = "관리자 로그인, 토큰 갱신, 현재 사용자 정보 조회 등 인증 관련 API")
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
            
            ### 주의사항
            - 관리자 권한(ADMIN)이 있는 계정만 로그인 가능
            - 비활성화된 계정은 로그인할 수 없습니다
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "로그인 성공",
                    summary = "정상적인 로그인 응답",
                    value = """
                    {
                      "success": true,
                      "message": "로그인 성공",
                      "status": 200,
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MjA5NjIwMDAsImV4cCI6MTcyMDk2NTYwMCwicm9sZSI6IkFETUlOIn0.example-jwt-token",
                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjEyMzRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MjA5NjIwMDAsImV4cCI6MTcyMTU2NjgwMH0.example-refresh-token",
                        "email": "admin1234@example.com"
                      }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "필수 필드 누락",
                        summary = "이메일 또는 비밀번호가 없는 경우",
                        value = """
                        {
                          "success": false,
                          "message": "이메일과 비밀번호는 필수입니다",
                          "errorCode": "VALIDATION_ERROR",
                          "status": 400
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "잘못된 이메일 형식",
                        summary = "올바르지 않은 이메일 형식인 경우",
                        value = """
                        {
                          "success": false,
                          "message": "올바른 이메일 형식이 아닙니다",
                          "errorCode": "INVALID_EMAIL_FORMAT",
                          "status": 400
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "잘못된 JSON 형식",
                        summary = "요청 본문이 올바른 JSON 형식이 아닌 경우",
                        value = """
                        {
                          "success": false,
                          "message": "요청 본문이 올바른 JSON 형식이 아닙니다",
                          "errorCode": "INVALID_JSON",
                          "status": 400
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "잘못된 인증 정보",
                        summary = "이메일 또는 비밀번호가 틀린 경우",
                        value = """
                        {
                          "success": false,
                          "message": "이메일 또는 비밀번호가 올바르지 않습니다",
                          "errorCode": "AUTHENTICATION_FAILED",
                          "status": 401
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 계정",
                        summary = "등록되지 않은 이메일인 경우",
                        value = """
                        {
                          "success": false,
                          "message": "등록되지 않은 이메일입니다",
                          "errorCode": "USER_NOT_FOUND",
                          "status": 401
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "비밀번호 불일치",
                        summary = "비밀번호가 틀린 경우",
                        value = """
                        {
                          "success": false,
                          "message": "비밀번호가 일치하지 않습니다",
                          "errorCode": "PASSWORD_MISMATCH",
                          "status": 401
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "관리자 권한 없음",
                        summary = "ADMIN 권한이 없는 사용자가 로그인 시도한 경우",
                        value = """
                        {
                          "success": false,
                          "message": "관리자 권한이 필요합니다. 현재 권한: USER",
                          "errorCode": "INSUFFICIENT_PRIVILEGES",
                          "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "비활성화된 계정",
                        summary = "계정이 비활성화된 경우",
                        value = """
                        {
                          "success": false,
                          "message": "비활성화된 계정입니다. 관리자에게 문의하세요",
                          "errorCode": "ACCOUNT_DISABLED",
                          "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "이메일 미인증",
                        summary = "이메일 인증이 완료되지 않은 경우",
                        value = """
                        {
                          "success": false,
                          "message": "이메일 인증이 필요합니다",
                          "errorCode": "EMAIL_NOT_VERIFIED",
                          "status": 403
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429", 
            description = "너무 많은 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "로그인 시도 횟수 초과",
                    summary = "짧은 시간 내에 너무 많은 로그인 시도를 한 경우",
                    value = """
                    {
                      "success": false,
                      "message": "로그인 시도 횟수를 초과했습니다. 15분 후 다시 시도해주세요",
                      "errorCode": "TOO_MANY_LOGIN_ATTEMPTS",
                      "status": 429,
                      "retryAfter": 900
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "토큰 생성 오류",
                        summary = "JWT 토큰 생성 중 오류가 발생한 경우",
                        value = """
                        {
                          "success": false,
                          "message": "로그인 처리 중 오류가 발생했습니다: 토큰 생성 실패",
                          "errorCode": "TOKEN_GENERATION_ERROR",
                          "status": 500
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "데이터베이스 오류",
                        summary = "데이터베이스 연결 문제가 발생한 경우",
                        value = """
                        {
                          "success": false,
                          "message": "로그인 처리 중 오류가 발생했습니다: 데이터베이스 연결 오류",
                          "errorCode": "DATABASE_ERROR",
                          "status": 500
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "시스템 오류",
                        summary = "예상치 못한 시스템 오류가 발생한 경우",
                        value = """
                        {
                          "success": false,
                          "message": "로그인 처리 중 시스템 오류가 발생했습니다",
                          "errorCode": "INTERNAL_ERROR",
                          "status": 500
                        }
                        """
                    )
                }
            )
        )
    })
    @PostMapping("/login")
    public void loginDoc(@RequestBody AdminLoginRequest loginRequest) {
        // 이 메서드는 실행되지 않습니다. Swagger 문서화 목적으로만 존재합니다.
        throw new UnsupportedOperationException("이 메서드는 Swagger 문서화 목적으로만 존재합니다.");
    }
}
