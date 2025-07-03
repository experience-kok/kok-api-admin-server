package com.example.adminservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT 서명에 사용되는 시크릿 키
     */
    private String secret = "secretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKey";
    
    /**
     * 액세스 토큰 만료 시간 (밀리초)
     * 기본값: 1시간 = 3,600,000ms
     */
    private long accessTokenExpiration = 3600000L;
    
    /**
     * 리프레시 토큰 만료 시간 (밀리초)
     * 기본값: 7일 = 604,800,000ms
     */
    private long refreshTokenExpiration = 604800000L;
}
