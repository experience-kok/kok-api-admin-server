package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.UserListResponseDTO;
import com.example.adminservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리 API", description = "관리자용 사용자 관리 API")
public class UserManagementController {

    private final UserRepository userRepository;

    @Operation(
        summary = "사용자 통계 조회", 
        description = "전체 사용자 통계를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            // 예시 응답 데이터 (실제로는 서비스에서 데이터 조회 필요)
            long totalUsers = userRepository.count();
            long adminUsers = userRepository.countByRole("ADMIN");
            long activeUsers = userRepository.countByActive(true);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("activeUsers", activeUsers);
            
            return ResponseEntity.ok(BaseResponse.success(stats, "사용자 통계 조회 성공"));
        } catch (Exception e) {
            log.error("사용자 통계 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 통계 조회 실패", "INTERNAL_ERROR", 500));
        }
    }
    
    @Operation(
        summary = "사용자 목록 조회", 
        description = "등록된 모든 사용자 목록을 페이지네이션하여 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<User> usersPage = userRepository.findAll(pageable);
            
            List<UserListResponseDTO> userDTOs = usersPage.getContent().stream()
                    .map(user -> UserListResponseDTO.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .role(user.getRole())
                            .provider(user.getProvider())
                            .accountType(user.getAccountType())
                            .active(user.getActive())
                            .emailVerified(user.getEmailVerified())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            
            // 페이지네이션 정보를 별도로 반환하는 새로운 응답 객체 사용
            return ResponseEntity.ok(BaseResponse.successPaged(
                userDTOs, 
                "사용자 목록 조회 성공",
                usersPage.getNumber(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("사용자 목록 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 목록 조회 실패", "INTERNAL_ERROR", 500));
        }
    }
    
    @Operation(
        summary = "사용자 상세 정보 조회", 
        description = "특정 사용자의 상세 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            
            UserListResponseDTO userDTO = UserListResponseDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .provider(user.getProvider())
                    .accountType(user.getAccountType())
                    .active(user.getActive())
                    .emailVerified(user.getEmailVerified())
                    .gender(user.getGender())
                    .age(user.getAge())
                    .phone(user.getPhone())
                    .profileImg(user.getProfileImg())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(userDTO, "사용자 상세 정보 조회 성공"));
        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 상세 정보 조회 실패: " + e.getMessage(), "USER_NOT_FOUND", 404));
        }
    }
    
    @Operation(
        summary = "사용자 활성화/비활성화", 
        description = "특정 사용자의 계정을 활성화하거나 비활성화합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean active
    ) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            
            user.setActive(active);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("active", user.getActive());
            
            String message = active ? "사용자 계정이 활성화되었습니다." : "사용자 계정이 비활성화되었습니다.";
            return ResponseEntity.ok(BaseResponse.success(response, message));
        } catch (Exception e) {
            log.error("사용자 상태 변경 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 상태 변경 실패: " + e.getMessage(), "USER_UPDATE_ERROR", 500));
        }
    }
    
    @Operation(
        summary = "사용자 삭제", 
        description = "특정 사용자를 시스템에서 완전히 삭제합니다. 이 작업은 되돌릴 수 없습니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            // 사용자 존재 여부 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            
            // 관리자 계정 삭제 방지 로직 (선택적)
            if ("ADMIN".equals(user.getRole())) {
                log.warn("관리자 계정 삭제 시도: {}", userId);
                return ResponseEntity.ok(BaseResponse.fail("관리자 계정은 삭제할 수 없습니다.", "ADMIN_DELETE_FORBIDDEN", 403));
            }
            
            // 사용자 정보 삭제 전 로그 기록
            log.info("사용자 삭제: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
            
            // 사용자 삭제
            userRepository.delete(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("email", user.getEmail());
            response.put("deleted", true);
            
            return ResponseEntity.ok(BaseResponse.success(response, "사용자가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 삭제 실패: " + e.getMessage(), "USER_DELETE_ERROR", 500));
        }
    }
}
