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
@Tag(name = "관리자 인증 API", description = "🔐 관리자 로그인, 토큰 갱신, 현재 사용자 정보 조회 등 인증 관련 API")
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
        tags = {"관리자 인증 API"}
    )
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
            
            ### 응답 형식
            ```json
            {
              "success": true,
              "message": "토큰이 갱신되었습니다.",
              "data": {
                "accessToken": "새로운_액세스_토큰"
              }
            }
            ```
            
            ### 주의사항
            - Refresh 토큰이 유효해야 합니다
            - 만료된 Refresh 토큰은 사용할 수 없습니다
            """,
        tags = {"관리자 인증 API"}
    )
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
