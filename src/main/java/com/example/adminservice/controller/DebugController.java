package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.security.JwtConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/debug")

public class DebugController {

    
    @GetMapping("/token")
    public ResponseEntity<?> getTestToken() {
        log.info("테스트 토큰 발급 API 호출됨");
        
        try {
            String userId = "1234";
            
            // 토큰 생성에 사용할 키
            Key key = Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            
            // 액세스 토큰 생성
            String accessToken = Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.ACCESS_TOKEN_EXPIRATION))
                    .claim("role", JwtConstants.ROLE_ADMIN)
                    .signWith(key)
                    .compact();
            
            // 리프레시 토큰 생성
            String refreshToken = Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.REFRESH_TOKEN_EXPIRATION))
                    .signWith(key)
                    .compact();
            
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("accessToken", accessToken);
            tokenInfo.put("refreshToken", refreshToken);
            tokenInfo.put("userId", userId);
            
            log.info("테스트 토큰 발급 성공: userId={}", userId);
            return ResponseEntity.ok(BaseResponse.success(tokenInfo, "테스트 토큰이 발급되었습니다"));
            
        } catch (Exception e) {
            log.error("테스트 토큰 발급 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.fail("토큰 발급 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
