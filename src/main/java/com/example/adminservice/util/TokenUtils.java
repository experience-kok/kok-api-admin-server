package com.example.adminservice.util;

import com.example.adminservice.exception.JwtValidationException;
import com.example.adminservice.exception.TokenErrorType;
import com.example.adminservice.exception.UnauthorizedException;
import com.example.adminservice.security.JwtUtil;
import com.example.adminservice.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtils {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출하고 사용자 ID를 반환
     * @param bearerToken Authorization 헤더 값 (Bearer 포함)
     * @return 토큰에서 추출한 사용자 ID
     * @throws UnauthorizedException 토큰이 유효하지 않은 경우
     */
    public Long getUserIdFromToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.warn("유효하지 않은 토큰 형식");
            throw new UnauthorizedException("유효하지 않은 토큰 형식입니다.");
        }

        String token = bearerToken.replace("Bearer ", "");

        try {
            // 만료 여부 먼저 확인
            Claims claims = jwtUtil.validateToken(token);

            // 블랙리스트 체크
            if (tokenService.isBlacklisted(token)) {
                log.warn("블랙리스트에 포함된 토큰");
                throw new UnauthorizedException("로그아웃된 토큰입니다.");
            }

            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰");
            throw new JwtValidationException("토큰이 만료되었습니다.", TokenErrorType.EXPIRED);
        } catch (Exception e) {
            log.error("토큰 파싱 중 오류: {}", e.getMessage());
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 토큰이 유효한지 확인
     * @param bearerToken Authorization 헤더 값 (Bearer 포함)
     * @return 토큰 유효 여부
     */
    public boolean isValidToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return false;
        }

        String token = bearerToken.replace("Bearer ", "");

        // 블랙리스트 체크
        if (tokenService.isBlacklisted(token)) {
            return false;
        }

        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}