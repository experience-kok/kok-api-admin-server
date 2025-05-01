package com.example.adminservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("1234");
        System.out.println("비밀번호 '1234'의 암호화된 값: " + encodedPassword);
    }
}
