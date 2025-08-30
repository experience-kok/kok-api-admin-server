package com.example.adminservice.service;

import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.AdminDTO;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.security.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndRole(username, UserRole.ADMIN)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), 
            user.getPassword(), 
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Transactional
    public AdminDTO getAdminByEmail(String email) {
        User user = userRepository.findByEmailAndRole(email, UserRole.ADMIN)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + email));
        
        return toDTO(user);
    }

    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmailAndRole(email, UserRole.ADMIN)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + email));
        
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Refresh 토큰을 검증하고 Claims를 추출합니다.
     */
    public Claims validateRefreshToken(String refreshToken) {
        try {
            Key key = Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e) {
            log.error("Refresh 토큰 검증 실패: {}", e.getMessage());
            throw new RuntimeException("유효하지 않은 Refresh 토큰입니다: " + e.getMessage());
        }
    }
    
    /**
     * 새로운 Access 토큰을 생성합니다.
     */
    public String generateAccessToken(String email) {
        Key key = Keys.hmacShaKeyFor(JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.ACCESS_TOKEN_EXPIRATION))
                .claim("role", "ADMIN")
                .signWith(key)
                .compact();
    }
    
    private AdminDTO toDTO(User user) {
        return AdminDTO.builder()
                .id(user.getId())
                .name(user.getNickname())
                .email(user.getEmail())
                .lastLoginAt(user.getUpdatedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
