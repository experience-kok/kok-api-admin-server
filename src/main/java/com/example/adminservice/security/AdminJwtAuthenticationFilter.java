package com.example.adminservice.security;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.AdminLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AdminJwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AdminJwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // 컨텍스트 경로를 제외한 경로 설정 (/auth/login)
        // 컨텍스트 경로(/admin-api)는 자동으로 처리됨
        setFilterProcessesUrl("/auth/login");
        log.info("AdminJwtAuthenticationFilter 초기화: 로그인 URL 설정 = /auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws AuthenticationException {
        log.info("로그인 요청 수신: {}", request.getRequestURI());
        try {
            AdminLoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), AdminLoginRequest.class);
            log.info("로그인 시도: email={}", loginRequest.getEmail());
            
            // 인증 처리
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            log.error("로그인 요청 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("로그인 요청 처리 중 오류가 발생했습니다", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                           FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        log.info("로그인 성공: email={}", user.getUsername());
        
        Key key = Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        
        // Access 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.ACCESS_TOKEN_EXPIRATION))
                .claim("role", JwtConstants.ROLE_ADMIN)
                .signWith(key)
                .compact();
                
        // Refresh 토큰 생성
        String refreshToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.REFRESH_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
        
        // 토큰 데이터
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", accessToken);
        tokenData.put("refreshToken", refreshToken);
        tokenData.put("email", user.getUsername());
        
        // BaseResponse 형식으로 응답 생성
        BaseResponse.Success<Map<String, Object>> responseBody = 
            BaseResponse.success(tokenData, "로그인 성공");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        log.warn("로그인 실패: {}", failed.getMessage());
        
        BaseResponse.Error errorResponse = 
            BaseResponse.fail("이메일 또는 비밀번호가 올바르지 않습니다.", "AUTHENTICATION_FAILED", 401);
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
