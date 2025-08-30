package com.example.adminservice.config;

import com.example.adminservice.constant.AccountType;
import com.example.adminservice.constant.Provider;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import com.example.adminservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        createUploadDirectories();
    }

    private void createDefaultAdminIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByEmailAndRole(adminEmail, UserRole.ADMIN);

        if (existingAdmin.isPresent()) {
            log.info("기존 관리자 계정이 존재합니다. email={}", adminEmail);
            return;
        }

        log.info("기본 관리자 계정을 생성합니다. email={}", adminEmail);

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword)) // BCrypt로 해시된 비밀번호 저장
                .nickname(adminName)
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(true)
                // 필수 필드 추가
                .provider(Provider.LOCAL.getValue())
                .socialId("admin_" + adminEmail)
                .accountType(AccountType.LOCAL)
                .gender(null) // gender 필드를 명시적으로 null로 설정
                .build();

        userRepository.save(admin);
        log.info("관리자 계정 생성 완료: id={}, email={}", admin.getId(), admin.getEmail());
    }
    
    private void createUploadDirectories() {
        try {
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get("uploads");
            Path bannerDir = Paths.get("uploads/banners");
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("업로드 디렉토리 생성: {}", uploadDir.toAbsolutePath());
            }
            
            if (!Files.exists(bannerDir)) {
                Files.createDirectories(bannerDir);
                log.info("배너 업로드 디렉토리 생성: {}", bannerDir.toAbsolutePath());
            }
            
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패", e);
        }
    }
}
