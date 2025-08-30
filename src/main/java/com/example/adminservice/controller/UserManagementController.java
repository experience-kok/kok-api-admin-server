package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.UserDetailDto;
import com.example.adminservice.dto.UserListResponseDTO;
import com.example.adminservice.dto.UserMemoUpdateRequest;
import com.example.adminservice.dto.UserSearchRequestDTO;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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
@Tag(name = "사용자 관리 API", description = "일반 사용자, 클라이언트, 관리자 계정 관리 API")
public class UserManagementController {

    private final UserRepository userRepository;
    private final UserManagementService userManagementService;


    @Operation(
            summary = "사용자 통계 정보 조회",
            description = """
            전체 사용자 통계 정보를 조회합니다.
            
            ### 제공 통계
            - 총 인원수: 등록된 전체 사용자 수
            - 유저수: USER 권한을 가진 사용자 수
            - 클라이언트수: CLIENT 권한을 가진 사용자 수  
            - 활성 사용자 수: 현재 활성화된 사용자 수
            - 비활성 사용자 수: 비활성화된 사용자 수
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 통계 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "일반적인 통계",
                                            summary = "사용자 현황 통계",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "사용자 통계 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "totalUsers": 1250,
                                                "userCount": 1000,
                                                "clientCount": 245,
                                                "activeUsers": 1180,
                                                "inactiveUsers": 70
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "초기 상태",
                                            summary = "서비스 초기 단계의 통계",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "사용자 통계 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "totalUsers": 12,
                                                "userCount": 7,
                                                "clientCount": 3,
                                                "activeUsers": 10,
                                                "inactiveUsers": 2
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    summary = "JWT 토큰이 만료된 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "토큰이 만료되었습니다. 다시 로그인 해주세요",
                                      "errorCode": "TOKEN_EXPIRED",
                                      "status": 401
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "관리자 권한 없음",
                                    summary = "ADMIN 권한이 없는 사용자가 접근한 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "사용자 통계 조회는 관리자만 가능합니다. 현재 권한: CLIENT",
                                      "errorCode": "FORBIDDEN",
                                      "status": 403
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "데이터베이스 오류",
                                            summary = "데이터베이스 연결 문제가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "사용자 통계 조회 실패: 데이터베이스 연결 오류",
                                              "errorCode": "INTERNAL_ERROR",
                                              "status": 500
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "통계 계산 오류",
                                            summary = "통계 계산 중 오류가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "사용자 통계 조회 실패: 통계 계산 중 오류가 발생했습니다",
                                              "errorCode": "INTERNAL_ERROR",
                                              "status": 500
                                            }
                                            """
                                    )
                            }
                    )
            )
    })

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            long totalUsers = userRepository.count();                         // 총 인원수
            long userCount = userRepository.countByRole(UserRole.USER);       // 유저수 (USER 역할)
            long clientCount = userRepository.countByRole(UserRole.CLIENT);   // 클라이언트수
            long activeUsers = userRepository.countByActive(true);
            long inactiveUsers = userRepository.countByActive(false);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);         // 총 인원수
            stats.put("userCount", userCount);           // 유저수 (USER 역할)
            stats.put("clientCount", clientCount);       // 클라이언트수
            stats.put("activeUsers", activeUsers);       // 활성 사용자수
            stats.put("inactiveUsers", inactiveUsers);   // 비활성 사용자수

            return ResponseEntity.ok(BaseResponse.success(stats, "사용자 통계 조회 성공"));
        } catch (Exception e) {
            log.error("사용자 통계 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 통계 조회 실패", "INTERNAL_ERROR", 500));
        }
    }

    @Operation(
            summary = "사용자 목록 조회 (페이지네이션)",
            description = """
            등록된 모든 사용자 목록을 페이지네이션하여 조회합니다.
            
            ### 쿼리 파라미터
            - page: 페이지 번호 (0부터 시작, 기본값: 0)
            - size: 페이지당 항목 수 (기본값: 10, 최대: 100)
            - sortBy: 정렬 기준 필드 (기본값: id)
            - sortDirection: 정렬 방향 (기본값: DESC)
            
            ### 정렬 옵션
            - id, email, nickname, createdAt, updatedAt, role
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "페이징된 사용자 목록",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "사용자 목록 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 1,
                                            "email": "user1@example.com",
                                            "nickname": "김사용자",
                                            "role": "USER",
                                            "provider": "GOOGLE",
                                            "accountType": "SOCIAL",
                                            "active": true,
                                            "emailVerified": true,
                                            "createdAt": "2025-07-14T10:00:00",
                                            "updatedAt": "2025-07-14T15:30:00"
                                          },
                                          {
                                            "id": 2,
                                            "email": "client@example.com",
                                            "nickname": "이클라이언트",
                                            "role": "CLIENT",
                                            "provider": "LOCAL",
                                            "accountType": "LOCAL",
                                            "active": true,
                                            "emailVerified": true,
                                            "createdAt": "2025-07-13T14:20:00",
                                            "updatedAt": "2025-07-14T09:15:00"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "totalPages": 125,
                                          "totalElements": 1250,
                                          "first": true,
                                          "last": false
                                        }
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<?> getUserList(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수 (최대 100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향 (ASC/DESC)")
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
                            .role(user.getRole().name())
                            .provider(user.getProvider())
                            .accountType(user.getAccountType().name())
                            .active(user.getActive())
                            .emailVerified(user.getEmailVerified())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(BaseResponse.successPaged(
                    userDTOs,
                    "사용자 목록 조회 성공",
                    usersPage
            ));
        } catch (Exception e) {
            log.error("사용자 목록 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 목록 조회 실패", "INTERNAL_ERROR", 500));
        }
    }

    @Operation(
            summary = "사용자 상세 정보 조회",
            description = """
            특정 사용자의 상세 정보를 조회합니다.
            
            ### 응답 정보
            - 기본 사용자 정보 (이메일, 닉네임, 권한 등)
            - 프로필 정보 (성별, 나이, 전화번호, 프로필 이미지)
            - 계정 상태 정보 (활성화, 이메일 인증 등)
            - 관리자 메모
            - 생성일, 수정일
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 상세 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "사용자 상세 정보",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "사용자 상세 정보 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "id": 1,
                                        "email": "user@example.com",
                                        "nickname": "김사용자",
                                        "role": "USER",
                                        "provider": "GOOGLE",
                                        "accountType": "SOCIAL",
                                        "active": true,
                                        "emailVerified": true,
                                        "gender": "MALE",
                                        "age": 28,
                                        "phone": "010-1234-5678",
                                        "profileImg": "https://example.com/profile/user1.jpg",
                                        "memo": "VIP 고객, 특별 관리 필요",
                                        "createdAt": "2025-07-14T10:00:00",
                                        "updatedAt": "2025-07-14T15:30:00",
                                        "platforms": null
                                      }
                                    }
                                    """
                            )
                    )
            )
    })

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId
    ) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            UserDetailDto userDTO = UserDetailDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole().name())
                    .provider(user.getProvider())
                    .accountType(user.getAccountType().name())
                    .active(user.getActive())
                    .emailVerified(user.getEmailVerified())
                    .gender(user.getGender())
                    .age(user.getAge())
                    .phone(user.getPhone())
                    .profileImg(user.getProfileImg())
                    .memo(user.getMemo())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .platforms(null)
                    .build();

            return ResponseEntity.ok(BaseResponse.success(userDTO, "사용자 상세 정보 조회 성공"));
        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 상세 정보 조회 실패: " + e.getMessage(), "USER_NOT_FOUND", 404));
        }
    }

    @Operation(
            summary = "사용자 메모 업데이트",
            description = """
            관리자가 특정 사용자에 대한 메모를 업데이트합니다.
            
            ### 기능
            - 사용자별 관리자 메모 작성/수정
            - 메모는 최대 1000자까지 허용
            - 빈 문자열로 메모 삭제 가능
            
            ### 요청 본문 예시
            ```json
            {
              "memo": "VIP 고객, 특별 관리 필요"
            }
            ```
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 메모 업데이트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "메모 업데이트 성공",
                                    summary = "사용자 메모가 성공적으로 업데이트된 경우",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "사용자 메모가 업데이트되었습니다",
                                      "status": 200,
                                      "data": {
                                        "userId": 1,
                                        "memo": "VIP 고객, 특별 관리 필요"                              
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @PutMapping("/{userId}/memo")
    public ResponseEntity<?> updateUserMemo(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "메모 업데이트 요청", required = true)
            @RequestBody UserMemoUpdateRequest request
    ) {
        try {
            String memo = request.getMemo();

            // 메모 길이 검증
            if (memo != null && memo.length() > 1000) {
                return ResponseEntity.ok(BaseResponse.fail("메모는 1000자 이하로 입력해주세요", "INVALID_PARAMETER", 400));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            user.setMemo(memo);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("memo", user.getMemo());

            return ResponseEntity.ok(BaseResponse.success(response, "사용자 메모가 업데이트되었습니다"));
        } catch (Exception e) {
            log.error("사용자 메모 업데이트 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 메모 업데이트 실패: " + e.getMessage(), "USER_MEMO_UPDATE_ERROR", 500));
        }
    }

    @Operation(
            summary = "사용자 활성화/비활성화 토글",
            description = """
            특정 사용자의 계정 상태를 토글합니다.
            
            ### 기능
            - 활성화된 사용자 → 비활성화
            - 비활성화된 사용자 → 활성화
            - 파라미터 없이 현재 상태를 반전시킵니다
            
            ### 주의사항
            - 비활성화된 사용자는 로그인할 수 없습니다
            - 관리자 계정은 비활성화할 수 없습니다 (선택적 제약)
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId
    ) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            // 현재 상태를 반전
            boolean newStatus = !user.getActive();
            user.setActive(newStatus);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("Id", user.getId());
            response.put("active", user.getActive());
            response.put("email", user.getEmail());
            response.put("nickname", user.getNickname());

            String message = newStatus ? "사용자 계정이 활성화되었습니다." : "사용자 계정이 비활성화되었습니다.";
            return ResponseEntity.ok(BaseResponse.success(response, message));
        } catch (Exception e) {
            log.error("사용자 상태 변경 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 상태 변경 실패: " + e.getMessage(), "USER_UPDATE_ERROR", 500));
        }
    }

    @Operation(
            summary = "사용자 롤을 CLIENT로 승급",
            description = """
            USER 롤을 가진 사용자를 CLIENT 롤로 승급시킵니다.
            
            ### 기능
            - USER → CLIENT 롤 변경
            - 승급 이력 로그 기록
            
            ### 제약사항
            - USER 롤인 사용자만 승급 가능
            - 이미 CLIENT나 ADMIN인 경우 에러 반환
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/{userId}/promote-to-client")
    public ResponseEntity<?> promoteToClient(
            @Parameter(description = "승급할 사용자 ID", required = true)
            @PathVariable Long userId
    ) {
        try {
            // 사용자 존재 여부 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            // 현재 롤 확인
            if (!UserRole.USER.equals(user.getRole())) {
                String message = String.format("USER 롤인 사용자만 CLIENT로 승급할 수 있습니다. 현재 롤: %s", user.getRole().name());
                return ResponseEntity.ok(BaseResponse.fail(message, "INVALID_ROLE", 400));
            }

            // 롤 변경 전 로그 기록
            log.info("사용자 롤 승급: userId={}, email={}, 이전 롤={}, 새 롤=CLIENT",
                     user.getId(), user.getEmail(), user.getRole().name());

            // USER → CLIENT로 롤 변경
            user.setRole(UserRole.CLIENT);
            userRepository.save(user);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("nickname", user.getNickname());
            response.put("previousRole", "USER");
            response.put("currentRole", user.getRole().name());
            response.put("promotedAt", user.getUpdatedAt());

            log.info("사용자 롤 승급 완료: userId={}, 새 롤={}", user.getId(), user.getRole().name());

            return ResponseEntity.ok(BaseResponse.success(response, "사용자가 성공적으로 CLIENT로 승급되었습니다."));

        } catch (Exception e) {
            log.error("사용자 롤 승급 중 오류: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 롤 승급 실패: " + e.getMessage(), "USER_PROMOTION_ERROR", 500));
        }
    }

    @Operation(
            summary = "사용자 삭제",
            description = """
            특정 사용자를 시스템에서 완전히 삭제합니다.
            
            ### 주의사항
            - 이 작업은 되돌릴 수 없습니다
            - 관리자 계정은 삭제할 수 없습니다
            - 사용자와 연관된 모든 데이터가 함께 삭제될 수 있습니다
            
            ### 권한
            - ADMIN 권한 필요
            - 최고 관리자 권한이 필요할 수 있습니다
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "삭제할 사용자 ID", required = true)
            @PathVariable Long userId
    ) {
        try {
            // 사용자 존재 여부 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            // 관리자 계정 삭제 방지 로직
            if (UserRole.ADMIN.equals(user.getRole())) {
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

    /**
     * 사용자 검색 내부 메서드
     */
    private Page<UserListResponseDTO> searchUsersInternal(String keyword, int page, int size, String role, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 권한 검증
        UserRole userRole = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 사용자 권한입니다: " + role + ". 사용 가능한 값: USER, CLIENT, ADMIN");
            }
        }

        Page<User> usersPage = userRepository.findByKeyword(keyword, userRole, active, pageable);

        List<UserListResponseDTO> userDTOs = usersPage.getContent().stream()
                .map(user -> UserListResponseDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .role(user.getRole().name())
                        .provider(user.getProvider())
                        .accountType(user.getAccountType().name())
                        .active(user.getActive())
                        .emailVerified(user.getEmailVerified())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, usersPage.getTotalElements());
    }
    @Operation(
            summary = "사용자 검색",
            description = """
                    키워드로 사용자를 검색합니다.
                    
                    ### 검색 대상
                    - 이메일
                    - 닉네임
                    - 전화번호
                    - 관리자 메모
                    
                    ### 필터 옵션
                    - `role`: 사용자 권한 (USER, CLIENT, ADMIN)
                    - `active`: 활성화 상태 (true/false)
                    
                    ### 사용 예시
                    - `GET /api/users/search?keyword=김`
                    - `GET /api/users/search?keyword=gmail&role=USER`
                    - `GET /api/users/search?keyword=010&active=true&page=1&size=20`
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "검색 성공",
                                    summary = "키워드 검색 결과",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "사용자 검색 성공",
                                      "status": 200,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 1,
                                            "email": "kimuser@gmail.com",
                                            "nickname": "김사용자",
                                            "role": "USER",
                                            "provider": "GOOGLE",
                                            "accountType": "SOCIAL",
                                            "active": true,
                                            "emailVerified": true,
                                            "gender": "MALE",
                                            "age": 28,
                                            "phone": "010-1234-5678",
                                            "profileImg": "https://example.com/profile.jpg",
                                            "createdAt": "2025-07-14T10:00:00",
                                            "updatedAt": "2025-07-14T15:30:00"
                                          },
                                          {
                                            "id": 5,
                                            "email": "kim.client@gmail.com",
                                            "nickname": "김클라이언트",
                                            "role": "CLIENT",
                                            "provider": "GOOGLE",
                                            "accountType": "SOCIAL",
                                            "active": true,
                                            "emailVerified": true,
                                            "gender": "FEMALE",
                                            "age": 32,
                                            "phone": "010-5678-1234",
                                            "profileImg": "https://example.com/profile2.jpg",
                                            "createdAt": "2025-07-10T14:20:00",
                                            "updatedAt": "2025-07-14T11:45:00"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "totalPages": 1,
                                          "totalElements": 2,
                                          "first": true,
                                          "last": true
                                        }
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "키워드 없음",
                                            summary = "검색 키워드가 없는 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "검색 키워드는 필수입니다",
                                              "errorCode": "INVALID_PARAMETER",
                                              "status": 400
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 권한",
                                            summary = "유효하지 않은 role 값",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "유효하지 않은 권한입니다: INVALID_ROLE",
                                              "errorCode": "INVALID_PARAMETER",
                                              "status": 400
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "message": "인증이 필요합니다",
                                      "errorCode": "UNAUTHORIZED",
                                      "status": 401
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "message": "관리자 권한이 필요합니다",
                                      "errorCode": "FORBIDDEN",
                                      "status": 403
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @Parameter(description = "검색 키워드 (이메일, 닉네임, 전화번호, 메모)", required = true, example = "김")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "사용자 권한 필터 (USER, CLIENT, ADMIN)", example = "USER")
            @RequestParam(required = false) String role,
            @Parameter(description = "활성화 상태 필터", example = "true")
            @RequestParam(required = false) Boolean active
    ) {
        try {
            // 입력값 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "검색 키워드는 필수입니다",
                        "INVALID_PARAMETER",
                        400
                ));
            }

            if (page < 0) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "페이지 번호는 0 이상이어야 합니다",
                        "INVALID_PARAMETER",
                        400
                ));
            }

            if (size < 1 || size > 100) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "페이지 크기는 1~100 사이여야 합니다",
                        "INVALID_PARAMETER",
                        400
                ));
            }

            // 검색 실행
            Page<UserListResponseDTO> users = userManagementService.searchUsers(
                    keyword.trim(), page, size, role, active
            );

            // BaseResponse.successPaged 사용 (기존 /users API와 동일)
            return ResponseEntity.ok(BaseResponse.successPaged(
                    users.getContent(),
                    "사용자 검색 성공",
                    users
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.fail(
                    e.getMessage(),
                    "INVALID_PARAMETER",
                    400
            ));
        } catch (Exception e) {
            log.error("사용자 검색 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "사용자 검색 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }
}