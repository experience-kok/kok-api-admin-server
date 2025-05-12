package com.example.adminservice.config;

import com.example.adminservice.domain.User;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.security.JwtConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin1234@example.com}")
    private String adminEmail;

    @Value("${admin.password:1234}")
    private String adminPassword;

    @Value("${admin.name:관리자}")
    private String adminName;

    @PostConstruct
    public void init() {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByEmailAndRole(adminEmail, JwtConstants.ROLE_ADMIN);

        if (existingAdmin.isPresent()) {
            log.info("기존 관리자 계정이 존재합니다. email={}", adminEmail);
            return;
        }

        log.info("기본 관리자 계정을 생성합니다. email={}", adminEmail);

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword)) // BCrypt로 해시된 비밀번호 저장
                .nickname(adminName)
                .role(JwtConstants.ROLE_ADMIN)
                .active(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                // 필수 필드 추가
                .provider("local")
                .socialId("admin_" + adminEmail)
                .accountType("LOCAL")
                .build();

        userRepository.save(admin);
        log.info("관리자 계정 생성 완료: id={}, email={}", admin.getId(), admin.getEmail());
    }
}