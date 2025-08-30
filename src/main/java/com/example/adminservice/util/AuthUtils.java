package com.example.adminservice.util;

import com.example.adminservice.domain.User;
import com.example.adminservice.exception.AuthenticationException;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 인증 관련 유틸리티 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUtils {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 문자열 (없으면 null)
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
    
    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);
            String subject = claims.getSubject();
            
            // subject가 이메일인 경우와 ID인 경우를 모두 처리
            if (subject.contains("@")) {
                // 이메일인 경우: 이메일로 사용자 조회해서 ID 반환
                User user = userRepository.findByEmail(subject)
                        .orElseThrow(() -> new AuthenticationException("사용자를 찾을 수 없습니다. 이메일: " + subject));
                return user.getId();
            } else {
                // 숫자 ID인 경우
                return Long.parseLong(subject);
            }
        } catch (NumberFormatException e) {
            log.error("토큰 subject를 숫자로 변환할 수 없음: {}", e.getMessage());
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
    }
    
    /**
     * JWT 토큰에서 사용자 정보를 조회합니다.
     * @param token JWT 토큰
     * @return 사용자 정보
     */
    public User getUserFromToken(String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);
            String subject = claims.getSubject();
            
            // subject가 이메일인 경우와 ID인 경우를 모두 처리
            if (subject.contains("@")) {
                // 이메일인 경우: 직접 이메일로 조회
                return userRepository.findByEmail(subject)
                        .orElseThrow(() -> new AuthenticationException("사용자를 찾을 수 없습니다. 이메일: " + subject));
            } else {
                // 숫자 ID인 경우: ID로 조회
                Long userId = Long.parseLong(subject);
                return userRepository.findById(userId)
                        .orElseThrow(() -> new AuthenticationException("사용자를 찾을 수 없습니다. ID: " + userId));
            }
        } catch (NumberFormatException e) {
            log.error("토큰 subject를 처리할 수 없음: {}", e.getMessage());
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            log.error("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
    }
    
    /**
     * Authorization 헤더에서 사용자 정보를 조회합니다.
     * @param authorizationHeader Authorization 헤더 값
     * @return 사용자 정보
     */
    public User getUserFromAuthHeader(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        if (token == null) {
            throw new AuthenticationException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }
        return getUserFromToken(token);
    }
    
    /**
     * 사용자 인증 정보를 담은 객체
     */
    public static class AuthInfo {
        private final String userId;
        private final String userName;
        
        public AuthInfo(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getUserName() {
            return userName;
        }
    }
    
    /**
     * Authorization 헤더에서 작성자 정보를 추출합니다.
     * @param authorizationHeader Authorization 헤더 값
     * @return 작성자 ID와 이름
     */
    public AuthInfo getAuthorInfoFromAuthHeader(String authorizationHeader) {
        User user = getUserFromAuthHeader(authorizationHeader);
        return new AuthInfo(user.getId().toString(), user.getNickname());
    }
}
