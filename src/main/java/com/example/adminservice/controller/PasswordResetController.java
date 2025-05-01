package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.domain.User;
import com.example.adminservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/reset")
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 비활성화

public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    
    @PostMapping("/password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {
        
        log.info("비밀번호 재설정 요청: email={}", email);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("사용자를 찾을 수 없음: email={}", email);
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail("해당 이메일의 사용자를 찾을 수 없습니다.", "USER_NOT_FOUND", 400));
        }
        
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("비밀번호 재설정 완료: email={}", email);
        return ResponseEntity.ok(BaseResponse.success(null, "비밀번호가 재설정되었습니다."));
    }
}
