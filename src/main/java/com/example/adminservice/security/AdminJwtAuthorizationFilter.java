package com.example.adminservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Slf4j
public class AdminJwtAuthorizationFilter extends BasicAuthenticationFilter {

    public AdminJwtAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
        log.info("AdminJwtAuthorizationFilter 초기화");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain chain) throws IOException, ServletException {
        log.debug("인증 필터 실행: URI={}", request.getRequestURI());
        
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음 또는 Bearer 토큰 아님");
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);

        if (authentication != null) {
            log.debug("인증 성공: principal={}", authentication.getPrincipal());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("인증 실패: 유효하지 않은 토큰");
        }
        
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        
        try {
            Key key = Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String username = claims.getSubject();
            String role = (String) claims.get("role");
            
            if (username != null) {
                log.debug("토큰 검증 성공: username={}, role={}", username, role);
                return new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }
            return null;
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return null;
        }
    }
}
