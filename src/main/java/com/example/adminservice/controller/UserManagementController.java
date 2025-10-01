package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.constant.UserRole;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.UserDetailDto;
import com.example.adminservice.dto.UserListResponseDTO;
import com.example.adminservice.dto.UserMemoUpdateRequest;
import com.example.adminservice.dto.UserCampaignActivityDto;
import com.example.adminservice.repository.UserRepository;
import com.example.adminservice.service.UserManagementService;
import com.example.adminservice.service.UserCampaignActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final UserCampaignActivityService userCampaignActivityService;

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
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            long totalUsers = userRepository.count();
            long userCount = userRepository.countByRole(UserRole.USER);
            long clientCount = userRepository.countByRole(UserRole.CLIENT);
            long activeUsers = userRepository.countByActive(true);
            long inactiveUsers = userRepository.countByActive(false);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("userCount", userCount);
            stats.put("clientCount", clientCount);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);

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
    @PutMapping("/{userId}/memo")
    public ResponseEntity<?> updateUserMemo(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "메모 업데이트 요청", required = true)
            @RequestBody UserMemoUpdateRequest request
    ) {
        try {
            String memo = request.getMemo();

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
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            if (!UserRole.USER.equals(user.getRole())) {
                String message = String.format("USER 롤인 사용자만 CLIENT로 승급할 수 있습니다. 현재 롤: %s", user.getRole().name());
                return ResponseEntity.ok(BaseResponse.fail(message, "INVALID_ROLE", 400));
            }

            log.info("사용자 롤 승급: userId={}, email={}, 이전 롤={}, 새 롤=CLIENT",
                     user.getId(), user.getEmail(), user.getRole().name());

            user.setRole(UserRole.CLIENT);
            userRepository.save(user);

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
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            if (UserRole.ADMIN.equals(user.getRole())) {
                log.warn("관리자 계정 삭제 시도: {}", userId);
                return ResponseEntity.ok(BaseResponse.fail("관리자 계정은 삭제할 수 없습니다.", "ADMIN_DELETE_FORBIDDEN", 403));
            }

            log.info("사용자 삭제: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

            userRepository.delete(user);

            return ResponseEntity.ok(BaseResponse.success(null, "사용자가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail("사용자 삭제 실패: " + e.getMessage(), "USER_DELETE_ERROR", 500));
        }
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

            Page<UserListResponseDTO> users = userManagementService.searchUsers(
                    keyword.trim(), page, size, role, active
            );

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

    @Operation(
            summary = "사용자 캠페인 활동 내역 조회",
            description = """
            사용자의 캠페인 활동 내역을 조회합니다. 사용자 타입에 따라 다른 응답을 제공합니다.
            
            
            ### 공통 기능
            - 페이징 지원 (기본 10개씩)
            - 상태별 필터링 지원
            - 최신순 정렬
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "캠페인 활동 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserCampaignActivityDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "USER 타입 응답",
                                            summary = "일반 사용자의 캠페인 신청 내역",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "사용자 캠페인 활동 내역 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "userId": 1,
                                                "userRole": "USER",
                                                "items": [
                                                  {
                                                    "id": 101,
                                                    "title": "카페 방문 체험 캠페인",
                                                    "company": "스타벅스 코리아",
                                                    "type": "방문형",
                                                    "statusText": "완료",
                                                    "createdAt": "2025-07-10T14:30:00",
                                                    "updatedAt": "2025-07-15T18:00:00",
                                                    "campaignId": 20,
                                                    "maxApplicants": 50,
                                                    "currentApplications": null,
                                                    "approvedBy": null,
                                                    "approvalDate": null,
                                                    "recruitmentPeriod": null
                                                  }
                                                ],
                                                "pagination": {
                                                  "pageNumber": 0,
                                                  "pageSize": 10,
                                                  "totalPages": 2,
                                                  "totalElements": 15,
                                                  "first": true,
                                                  "last": false
                                                }
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "CLIENT 타입 응답",
                                            summary = "클라이언트의 생성 캠페인 내역",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "사용자 캠페인 활동 내역 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "userId": 2,
                                                "userRole": "CLIENT",
                                                "items": [
                                                  {
                                                    "id": 25,
                                                    "title": "신상품 리뷰 캠페인",
                                                    "company": "올리브영",
                                                    "campaignType": "블로그",
                                                    "statusText": "승인됨",
                                                    "createdAt": "2025-07-08T09:00:00",
                                                    "updatedAt": "2025-07-09T15:30:00",
                                                    "campaignId": null,
                                                    "maxApplicants": null,
                                                    "currentApplications": 23,
                                                    "approvedBy": "관리자",
                                                    "approvalDate": "2025-07-09T15:30:00",
                                                    "recruitmentPeriod": "2025-07-10 ~ 2025-07-20"
                                                  },
                                                  {
                                                    "id": 22,
                                                    "title": "화장품 체험 캠페인",
                                                    "company": "아모레퍼시픽",
                                                    "campaignType": "인스타",
                                                    "statusText": "만료됨",
                                                    "createdAt": "2025-06-15T14:20:00",
                                                    "updatedAt": "2025-06-16T10:30:00",
                                                    "campaignId": null,
                                                    "maxApplicants": null,
                                                    "currentApplications": 45,
                                                    "approvedBy": "관리자",
                                                    "approvalDate": "2025-06-16T10:30:00",
                                                    "recruitmentPeriod": "2025-06-20 ~ 2025-07-01"
                                                  }
                                                ],
                                                "pagination": {
                                                  "pageNumber": 0,
                                                  "pageSize": 10,
                                                  "totalPages": 2,
                                                  "totalElements": 12,
                                                  "first": true,
                                                  "last": false
                                                }
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                            value = """
                            {
                              "success": false,
                              "message": "사용자를 찾을 수 없습니다: 999",
                              "errorCode": "USER_NOT_FOUND",
                              "status": 404
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
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "message": "유효하지 않은 상태입니다: INVALID_STATUS",
                                      "errorCode": "INVALID_PARAMETER",
                                      "status": 400
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{userId}/activities")
    public ResponseEntity<?> getUserCampaignActivities(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "상태 필터 (USER: APPLIED/PENDING/SELECTED/REJECTED/COMPLETED, CLIENT: PENDING/APPROVED/REJECTED/EXPIRED)")
            @RequestParam(required = false) String status
    ) {
        try {
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

            Pageable pageable = PageRequest.of(page, size);
            UserCampaignActivityDto result = userCampaignActivityService.getUserCampaignActivities(userId, pageable, status);

            return ResponseEntity.ok(BaseResponse.success(result, "사용자 캠페인 활동 내역 조회 성공"));

        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("사용자를 찾을 수 없습니다")) {
                return ResponseEntity.ok(BaseResponse.fail(message, "USER_NOT_FOUND", 404));
            } else if (message.contains("유효하지 않은")) {
                return ResponseEntity.ok(BaseResponse.fail(message, "INVALID_PARAMETER", 400));
            } else {
                log.error("사용자 캠페인 활동 내역 조회 중 오류 발생: userId={}, error={}", userId, message, e);
                return ResponseEntity.ok(BaseResponse.fail(
                        "캠페인 활동 내역 조회 실패: " + message,
                        "INTERNAL_ERROR",
                        500
                ));
            }
        } catch (Exception e) {
            log.error("사용자 캠페인 활동 내역 조회 중 예기치 않은 오류 발생: userId={}", userId, e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 활동 내역 조회 실패: 서버 내부 오류",
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }
}
