package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.AdminDTO;
import com.example.adminservice.dto.RefreshTokenRequest;
import com.example.adminservice.exception.TokenRefreshException;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.security.JwtUtil;
import com.example.adminservice.service.AdminService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "관리자 인증 API", description = "관리자 로그인, 토큰 갱신, 현재 사용자 정보 조회 등 인증 관련 API")
public class AdminAuthController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "현재 로그인한 관리자 정보 조회", 
        description = """
            현재 JWT 토큰으로 인증된 관리자의 상세 정보를 반환합니다.
            
            ### 응답 정보
            - 관리자 ID, 이메일, 닉네임
            - 마지막 로그인 시간
            - 계정 생성일
            
            ### 주의사항
            - 유효한 JWT 토큰이 필요합니다
            - 관리자 권한(ADMIN)이 있는 계정만 접근 가능합니다
            """,
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "관리자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "현재 로그인한 관리자 정보",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "관리자 정보 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "id": 3,
                                        "email": "admin1234@example.com",
                                        "name": "관리자",
                                        "email": "admin1234@example.com",
                                        "lastLoginAt": "2025-08-18T16:18:50.014456",
                                        "createdAt": "2025-08-03T17:41:24.816406",
                                        "profileImg": null
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "토큰 없음",
                                            summary = "Authorization 헤더가 없는 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "인증된 관리자가 없습니다",
                                              "errorCode": "UNAUTHORIZED",
                                              "status": 401
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "토큰 만료",
                                            summary = "JWT 토큰이 만료된 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "토큰이 만료되었습니다. 다시 로그인 해주세요",
                                              "errorCode": "TOKEN_EXPIRED",
                                              "status": 401
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "유효하지 않은 토큰",
                                            summary = "잘못된 형식의 JWT 토큰인 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "유효하지 않은 토큰입니다",
                                              "errorCode": "INVALID_TOKEN",
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
                            examples = @ExampleObject(
                                    name = "관리자 권한 없음",
                                    summary = "ADMIN 권한이 없는 사용자가 접근한 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "관리자 권한이 필요합니다. 현재 권한: USER",
                                      "errorCode": "FORBIDDEN",
                                      "status": 403
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관리자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "관리자 정보 없음",
                                    summary = "토큰에 포함된 관리자 정보가 DB에 없는 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "관리자 정보를 찾을 수 없습니다",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
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
                                            name = "데이터베이스 오류",
                                            summary = "데이터베이스 연결 문제가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "관리자 정보 조회 실패: 데이터베이스 연결 오류",
                                              "errorCode": "INTERNAL_ERROR",
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
                                              "message": "관리자 정보 조회 실패: 시스템 오류가 발생했습니다",
                                              "errorCode": "INTERNAL_ERROR",
                                              "status": 500
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(BaseResponse.fail("인증된 관리자가 없습니다.", "UNAUTHORIZED", 401));
        }
        
        try {
            String email = principal.getName(); // 이메일이 principal의 name으로 저장됨
            adminService.updateLastLogin(email);
            
            AdminDTO adminDTO = adminService.getAdminByEmail(email);
            
            return ResponseEntity.ok(BaseResponse.success(adminDTO, "관리자 정보 조회 성공"));
        } catch (Exception e) {
            log.error("관리자 정보 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("관리자 정보 조회 실패", "INTERNAL_ERROR", 500));
        }
    }
    
    @Operation(
        summary = "JWT 토큰 재발급", 
        description = """
            Refresh 토큰을 사용하여 새로운 Access 토큰을 발급받습니다.
            
            ### 사용 시기
            - Access 토큰이 만료되었을 때
            - 토큰 갱신이 필요할 때
            
            ### 요청 형식
            ```json
            {
              "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
            }
            ```
            
            ### 주의사항
            - Refresh 토큰이 유효해야 합니다
            - 만료된 Refresh 토큰은 사용할 수 없습니다
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "새로운 Access 토큰이 발급된 경우",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "토큰이 갱신되었습니다",
                                      "status": 200,
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcyMDk2MjAwMCwiZXhwIjoxNzIwOTY1NjAwfQ.newTokenSignature",
                                        "tokenType": "Bearer",
                                        "expiresIn": 3600
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
                                            summary = "refreshToken 필드가 없는 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "Refresh 토큰은 필수입니다",
                                              "errorCode": "VALIDATION_ERROR",
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
                                            name = "유효하지 않은 Refresh 토큰",
                                            summary = "Refresh 토큰이 유효하지 않은 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "유효하지 않은 Refresh 토큰입니다",
                                              "errorCode": "INVALID_REFRESH_TOKEN",
                                              "status": 401
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "만료된 Refresh 토큰",
                                            summary = "Refresh 토큰이 만료된 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "Refresh 토큰이 만료되었습니다. 다시 로그인 해주세요",
                                              "errorCode": "REFRESH_TOKEN_EXPIRED",
                                              "status": 401
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 토큰 형식",
                                            summary = "토큰 형식이 잘못된 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "토큰 형식이 올바르지 않습니다",
                                              "errorCode": "MALFORMED_TOKEN",
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
                            examples = @ExampleObject(
                                    name = "관리자 권한 없음",
                                    summary = "토큰에 포함된 사용자가 관리자가 아닌 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "관리자 권한이 필요합니다",
                                      "errorCode": "FORBIDDEN",
                                      "status": 403
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "관리자 계정 없음",
                                    summary = "토큰에 포함된 관리자가 DB에 존재하지 않는 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "관리자 계정을 찾을 수 없습니다",
                                      "errorCode": "USER_NOT_FOUND",
                                      "status": 404
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
                                            summary = "새로운 토큰 생성 중 오류가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "토큰 재발급 중 오류가 발생했습니다",
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
                                              "message": "토큰 재발급 중 데이터베이스 오류가 발생했습니다",
                                              "errorCode": "INTERNAL_ERROR",
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
                                              "message": "토큰 재발급 중 시스템 오류가 발생했습니다",
                                              "errorCode": "INTERNAL_ERROR",
                                              "status": 500
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        try {
            // Refresh 토큰 검증
            Claims claims;
            try {
                claims = adminService.validateRefreshToken(request.getRefreshToken());
            } catch (Exception e) {
                log.warn("리프레시 토큰 검증 실패: {}", e.getMessage());
                throw TokenRefreshException.invalidRefreshToken();
            }
            
            String email = claims.getSubject();
            
            // 관리자 권한 확인
            userRepository.findByEmailAndRole(email, UserRole.ADMIN)
                    .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + email));
            
            // 새 Access 토큰 발급
            String newAccessToken = adminService.generateAccessToken(email);
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("tokenType", "Bearer");
            tokenData.put("expiresIn", 3600); // 1시간
            
            log.info("토큰 재발급 성공: email={}", email);
            return ResponseEntity.ok(BaseResponse.success(tokenData, "토큰이 갱신되었습니다."));
        } catch (TokenRefreshException e) {
            // TokenRefreshException은 GlobalExceptionHandler에서 처리됨
            throw e;
        } catch (UsernameNotFoundException e) {
            log.warn("관리자 계정 없음: {}", e.getMessage());
            throw TokenRefreshException.invalidRefreshToken();
        } catch (Exception e) {
            log.error("토큰 재발급 중 오류: {}", e.getMessage(), e);
            throw TokenRefreshException.tokenRefreshError("토큰 재발급 중 오류가 발생했습니다.");
        }
    }
}
