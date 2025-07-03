package com.example.adminservice.util;

import com.example.adminservice.domain.User;
import com.example.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기존 평문 비밀번호를 BCrypt 해시로 마이그레이션하는 유틸리티 클래스
 * 애플리케이션 시작 시 자동으로 실행됩니다.
 * 
 * 비밀번호 길이가 60자 미만인 경우 해시되지 않은 평문 비밀번호로 간주합니다.
 * (BCrypt 해시는 항상 60자 길이입니다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class PasswordHashMigrationUtil {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateUnhashedPasswords() {
        log.info("평문 비밀번호 해시 마이그레이션 작업 시작");
        
        // 비밀번호가 있는 모든 사용자 조회
        List<User> users = userRepository.findByPasswordIsNotNull();
        
        int migratedCount = 0;
        
        for (User user : users) {
            String password = user.getPassword();
            
            // 비밀번호가 해시되지 않은 경우 (BCrypt 해시는 60자)
            if (password != null && password.length() < 60) {
                log.debug("사용자 비밀번호 해시 처리: id={}, email={}", user.getId(), user.getEmail());
                user.setPassword(passwordEncoder.encode(password));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                migratedCount++;
            }
        }
        
        log.info("비밀번호 해시 마이그레이션 완료: {}개 계정 업데이트됨", migratedCount);
    }
}
