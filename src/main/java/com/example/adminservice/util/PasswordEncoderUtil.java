package com.example.adminservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 비밀번호 인코딩 유틸리티 클래스
 * 개발 및 테스트를 위한 클래스입니다.
 */
public class PasswordEncoderUtil {
    /**
     * 주어진 비밀번호를 해시하고 출력합니다.
     * 개발자 도구로 사용하는 메인 메서드입니다.
     */
    public static void main(String[] args) {
        // 기본 인코더 생성 (기본 강도: 10)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 테스트용 비밀번호 목록
        String[] testPasswords = {"1234", "admin1234", "password123", "supersecret"};
        
        System.out.println("===== BCrypt 비밀번호 해시 테스트 =====");
        for (String password : testPasswords) {
            String encodedPassword = encoder.encode(password);
            System.out.println("원본: '" + password + "'");
            System.out.println("해시: " + encodedPassword);
            System.out.println("검증: " + encoder.matches(password, encodedPassword));
            System.out.println();
        }
        
        // 강도 설정이 다른 인코더
        BCryptPasswordEncoder strongerEncoder = new BCryptPasswordEncoder(12);
        String password = "1234";
        String encodedPassword = strongerEncoder.encode(password);
        System.out.println("===== 강화된 BCrypt (강도 12) =====");
        System.out.println("원본: '" + password + "'");
        System.out.println("해시: " + encodedPassword);
        System.out.println("검증: " + strongerEncoder.matches(password, encodedPassword));
    }
    
    /**
     * 비밀번호를 해시합니다.
     * @param rawPassword 원본 비밀번호
     * @return 해시된 비밀번호
     */
    public static String encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }
    
    /**
     * 비밀번호가 해시와 일치하는지 확인합니다.
     * @param rawPassword 확인할 원본 비밀번호
     * @param encodedPassword 해시된 비밀번호
     * @return 일치 여부
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}
