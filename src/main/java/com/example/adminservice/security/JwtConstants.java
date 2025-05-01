package com.example.adminservice.security;

/**
 * JWT 관련 상수 값을 정의하는 클래스입니다.
 * 개발 환경과 배포 환경에서 동일한 설정을 사용하기 위해 하드코딩된 값을 사용합니다.
 */
public class JwtConstants {
    
    /**
     * JWT 서명에 사용되는 시크릿 키입니다.
     * 실무에서는 이 값을 환경 변수나 보안 저장소에서 가져오는 것이 좋습니다.
     */
    public static final String JWT_SECRET = "secretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKeysecretKey";
    
    /**
     * 액세스 토큰 만료 시간 (밀리초)
     * 현재 값: 1시간 = 3,600,000ms
     */
    public static final long ACCESS_TOKEN_EXPIRATION = 3600000;
    
    /**
     * 리프레시 토큰 만료 시간 (밀리초)
     * 현재 값: 7일 = 604,800,000ms
     */
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000;
    
    /**
     * 관리자 역할 상수
     */
    public static final String ROLE_ADMIN = "ADMIN";
}
