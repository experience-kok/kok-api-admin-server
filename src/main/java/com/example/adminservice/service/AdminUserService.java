package com.example.adminservice.service;

import com.example.adminservice.domain.User;
import com.example.adminservice.dto.AdminUserDTO;
import com.example.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 사용자 목록을 조회합니다.
     * @return 사용자 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자를 ID로 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자 DTO (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<AdminUserDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto);
    }

    /**
     * 관리자 권한을 가진 사용자를 이메일로 조회합니다.
     * @param email 이메일
     * @return 관리자 DTO (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<AdminUserDTO> getAdminByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> "ADMIN".equals(user.getRole()))
                .map(this::convertToDto);
    }

    /**
     * 사용자 계정의 활성화 상태를 변경합니다.
     * @param userId 사용자 ID
     * @param active 활성화 여부
     * @return 변경된 사용자 DTO
     */
    @Transactional
    public AdminUserDTO updateUserActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        return convertToDto(savedUser);
    }

    /**
     * 사용자 엔티티를 DTO로 변환합니다.
     * @param user 사용자 엔티티
     * @return 사용자 DTO
     */
    private AdminUserDTO convertToDto(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getNickname())
                .role(user.getRole())
                .active(user.getActive())
                .provider(user.getProvider())
                .accountType(user.getAccountType())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
