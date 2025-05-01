package com.example.adminservice.security;

import com.example.adminservice.exception.JwtValidationException;
import com.example.adminservice.exception.TokenErrorType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessExpiration);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshExpiration);
    }

    private String createToken(Long userId, long expiration) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰을 검증하고 클레임을 반환합니다. 만료된 토큰은 예외가 발생합니다.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("토큰이 만료되었습니다.", TokenErrorType.EXPIRED);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            throw new JwtValidationException("잘못되었거나 위조된 토큰입니다.", TokenErrorType.INVALID);
        } catch (Exception e) {
            throw new JwtValidationException("알 수 없는 JWT 오류", TokenErrorType.UNKNOWN);
        }
    }
    
    /**
     * 토큰 만료 여부와 관계없이 클레임을 추출합니다. 만료된 토큰도 클레임을 반환합니다.
     */
    public Claims getClaimsIgnoreExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 클레임 반환
            return e.getClaims();
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            throw new JwtValidationException("잘못되었거나 위조된 토큰입니다.", TokenErrorType.INVALID);
        } catch (Exception e) {
            throw new JwtValidationException("알 수 없는 JWT 오류", TokenErrorType.UNKNOWN);
        }
    }
    
    /**
     * 일반적인 토큰 검증 및 클레임 추출
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("토큰이 만료되었습니다.", TokenErrorType.EXPIRED);
        } catch (Exception e) {
            throw new JwtValidationException("유효하지 않은 토큰입니다.", TokenErrorType.INVALID);
        }
    }
}