package com.example.adminservice.util;

import org.springframework.stereotype.Component;

/**
 * 비밀번호 해싱을 위한 유틸리티 클래스
 * Spring Security의 BCryptPasswordEncoder를 감싸서 사용
 */
@Component
public class BCryptPasswordEncoder {
    
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;
    
    public BCryptPasswordEncoder() {
        // 기본 강도(10)로 인코더 생성
        this.encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
    
    /**
     * 비밀번호를 해싱합니다.
     *
     * @param rawPassword 원본 비밀번호
     * @return 해싱된 비밀번호
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 입력된 비밀번호가 저장된 해시와 일치하는지 검증합니다.
     *
     * @param rawPassword 검증할 원본 비밀번호
     * @param encodedPassword 저장된 해시 비밀번호
     * @return 일치 여부
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
