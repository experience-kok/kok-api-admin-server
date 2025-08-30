package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.NotificationRequest;
import com.example.adminservice.dto.NotificationResponse;
import com.example.adminservice.service.NotificationService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "실시간 알림 서비스 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "새 알림 생성",
            description = """
            새로운 알림을 생성하고 실시간으로 전송합니다. (관리자 권한 필요)
            
            ### 요청 필드 설명
            - **userId**: 알림을 받을 사용자의 ID (필수)
            - **notificationType**: 알림 타입 (필수)
              - SYSTEM_NOTICE: 시스템 공지
              - CAMPAIGN_APPROVED: 캠페인 승인
              - CAMPAIGN_REJECTED: 캠페인 거절
              - CAMPAIGN_PENDING: 캠페인 대기
            - **title**: 알림 제목, 최대 200자 (필수)
            - **message**: 알림 내용 (필수)
            - **relatedEntityId**: 관련 엔티티 ID (선택, 캠페인 ID 등)
            - **relatedEntityType**: 관련 엔티티 타입 (선택)
              - CAMPAIGN: 캠페인
              - SYSTEM: 시스템
              - USER: 사용자
            
            ### 응답 정보
            생성된 알림의 상세 정보를 반환합니다.
            """,
            security = { @SecurityRequirement(name = "bearerAuth") },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "시스템 공지 알림",
                                            value = """
                                            {
                                              "userId": 1,
                                              "notificationType": "SYSTEM_NOTICE",
                                              "title": "시스템 점검 안내",
                                              "message": "2024년 8월 1일 새벽 2시부터 4시까지 시스템 점검이 있습니다.",
                                              "relatedEntityId": null,
                                              "relatedEntityType": "SYSTEM"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "캠페인 승인 알림",
                                            value = """
                                            {
                                              "userId": 1,
                                              "notificationType": "CAMPAIGN_APPROVED",
                                              "title": "캠페인 승인 완료",
                                              "message": "귀하의 캠페인 '여름 프로모션'이 승인되었습니다.",
                                              "relatedEntityId": 123,
                                              "relatedEntityType": "CAMPAIGN"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "캠페인 거절 알림",
                                            value = """
                                            {
                                              "userId": 1,
                                              "notificationType": "CAMPAIGN_REJECTED",
                                              "title": "캠페인 검토 결과",
                                              "message": "캠페인이 검토 기준에 부합하지 않아 거절되었습니다. 상세 내용은 마이페이지에서 확인하세요.",
                                              "relatedEntityId": 124,
                                              "relatedEntityType": "CAMPAIGN"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", 
                    description = "알림 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "알림이 성공적으로 생성되었습니다.",
                                      "status": 201,
                                      "data": {
                                        "id": 42,
                                        "userId": 1,
                                        "notificationType": "CAMPAIGN_APPROVED",
                                        "title": "캠페인 승인 완료",
                                        "message": "귀하의 캠페인 '여름 프로모션'이 승인되었습니다.",
                                        "relatedEntityId": 123,
                                        "relatedEntityType": "CAMPAIGN",
                                        "isRead": false,
                                        "createdAt": "2024-07-31T15:30:00",
                                        "readAt": null
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락, 유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 권한 필요)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<?> createNotification(
            @Parameter(
                    description = "알림 생성 요청 정보",
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "기본 예시",
                                            value = """
                                            {
                                              "userId": 1,
                                              "notificationType": "SYSTEM_NOTICE",
                                              "title": "테스트 알림",
                                              "message": "실시간 알림 테스트입니다.",
                                              "relatedEntityId": null,
                                              "relatedEntityType": "SYSTEM"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody NotificationRequest request
    ) {
        try {
            log.info("알림 생성 요청: userId={}, type={}", request.getUserId(), request.getNotificationType());

            NotificationResponse response = notificationService.createNotification(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(response, "알림이 성공적으로 생성되었습니다."));
        } catch (Exception e) {
            log.error("알림 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("알림 생성에 실패했습니다.", "NOTIFICATION_CREATION_FAILED", 500));
        }
    }

    @Operation(
            summary = "사용자 알림 목록 조회",
            description = """
            특정 사용자의 모든 알림을 페이징으로 조회합니다.
            
            ### 경로 파라미터
            - **userId**: 조회할 사용자의 ID
            
            ### 쿼리 파라미터 (페이징 및 정렬)
            - **page**: 페이지 번호 (0부터 시작, 기본값: 0)
            - **size**: 페이지당 항목 수 (기본값: 20, 최대: 100)
            - **sort**: 정렬 기준 (기본값: createdAt,desc)
              - `createdAt,desc`: 생성일시 기준 최신순 (기본값)
              - `createdAt,asc`: 생성일시 기준 오래된순
              - `readAt,desc`: 읽은 시간 기준 최신순
              - `readAt,asc`: 읽은 시간 기준 오래된순
              - `title,asc`: 제목 기준 가나다순
              - `title,desc`: 제목 기준 역순
            
            ### 예시 요청
            - `GET /api/notifications/users/1` - 기본 설정 (page=0, size=20, 최신순)
            - `GET /api/notifications/users/1?page=1&size=10` - 2페이지, 10개씩
            - `GET /api/notifications/users/1?sort=readAt,asc` - 읽은 시간 오름차순
            - `GET /api/notifications/users/1?page=0&size=50&sort=title,asc` - 제목순 정렬
            
            ### 응답 정보
            - **notifications**: 알림 목록 배열
            - **totalElements**: 전체 알림 개수
            - **totalPages**: 전체 페이지 수
            - **currentPage**: 현재 페이지 번호 (0부터 시작)
            - **size**: 페이지 크기
            - **first**: 첫 번째 페이지 여부
            - **last**: 마지막 페이지 여부
            - **numberOfElements**: 현재 페이지의 실제 항목 수
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "알림 목록을 성공적으로 조회했습니다.",
                                      "status": 200,
                                      "data": {
                                        "notifications": [
                                          {
                                            "id": 45,
                                            "userId": 1,
                                            "notificationType": "CAMPAIGN_APPROVED",
                                            "title": "캠페인 승인 완료",
                                            "message": "귀하의 캠페인 '여름 프로모션'이 승인되었습니다.",
                                            "relatedEntityId": 123,
                                            "relatedEntityType": "CAMPAIGN",
                                            "isRead": false,
                                            "createdAt": "2024-07-31T16:00:00",
                                            "readAt": null
                                          },
                                          {
                                            "id": 44,
                                            "userId": 1,
                                            "notificationType": "SYSTEM_NOTICE",
                                            "title": "시스템 점검 안내",
                                            "message": "2024년 8월 1일 새벽 2시부터 4시까지 시스템 점검이 있습니다.",
                                            "relatedEntityId": null,
                                            "relatedEntityType": "SYSTEM",
                                            "isRead": true,
                                            "createdAt": "2024-07-31T15:30:00",
                                            "readAt": "2024-07-31T15:45:00"
                                          }
                                        ],
                                        "totalElements": 25,
                                        "totalPages": 2,
                                        "currentPage": 0,
                                        "size": 20,
                                        "first": true,
                                        "last": false,
                                        "numberOfElements": 20
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (페이지 파라미터 오류, 정렬 기준 오류)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserNotifications(
            @Parameter(
                    description = "조회할 사용자 ID", 
                    example = "1",
                    required = true
            )
            @PathVariable Long userId,
            
            @Parameter(
                    description = "페이지 번호 (0부터 시작)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                    description = "페이지당 항목 수 (최대 100)",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                    description = "정렬 기준 (필드명,방향)",
                    examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "최신순", value = "createdAt,desc"),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "오래된순", value = "createdAt,asc"),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "읽은시간순", value = "readAt,desc"),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "제목순", value = "title,asc")
                    }
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        try {
            log.info("사용자 알림 목록 조회: userId={}, page={}, size={}, sort={}", userId, page, size, sort);

            // 페이지 크기 제한 (최대 100)
            if (size > 100) {
                size = 100;
            }
            if (size < 1) {
                size = 20; // 기본값
            }

            // 정렬 파라미터 파싱
            Sort sortObj;
            try {
                String[] sortParams = sort.split(",");
                String sortField = sortParams[0];
                Sort.Direction direction = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) 
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                
                // 허용된 정렬 필드 검증
                if (!isValidSortField(sortField)) {
                    sortField = "createdAt"; // 기본값
                    direction = Sort.Direction.DESC;
                }
                
                sortObj = Sort.by(direction, sortField);
            } catch (Exception e) {
                log.warn("정렬 파라미터 파싱 실패, 기본값 사용: sort={}", sort);
                sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
            }

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, sortObj);

            Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);

            Map<String, Object> responseData = Map.of(
                    "notifications", notifications.getContent(),
                    "totalElements", notifications.getTotalElements(),
                    "totalPages", notifications.getTotalPages(),
                    "currentPage", notifications.getNumber(),
                    "size", notifications.getSize(),
                    "first", notifications.isFirst(),
                    "last", notifications.isLast(),
                    "numberOfElements", notifications.getNumberOfElements()
            );

            return ResponseEntity.ok(
                    BaseResponse.success(responseData, "알림 목록을 성공적으로 조회했습니다.")
            );
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail("잘못된 요청: " + e.getMessage(), "INVALID_PARAMETER", 400));
        } catch (Exception e) {
            log.error("알림 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("알림 목록 조회에 실패했습니다.", "NOTIFICATION_FETCH_FAILED", 500));
        }
    }

    /**
     * 정렬 필드 유효성 검증
     */
    private boolean isValidSortField(String field) {
        return "createdAt".equals(field) || "readAt".equals(field) || "title".equals(field) || "id".equals(field);
    }

    @Operation(
            summary = "읽지 않은 알림 조회",
            description = """
            특정 사용자의 읽지 않은 알림 목록을 조회합니다.
            
            ### 경로 파라미터
            - **userId**: 조회할 사용자의 ID
            
            ### 응답 정보
            - **notifications**: 읽지 않은 알림 목록 (최신순)
            - **unreadCount**: 읽지 않은 알림 총 개수
            
            ### 특징
            - 페이징 없이 모든 읽지 않은 알림 반환
            - 최신 알림부터 정렬되어 반환
            - isRead가 false인 알림들만 포함
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "읽지 않은 알림 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "읽지 않은 알림을 성공적으로 조회했습니다.",
                                      "status": 200,
                                      "data": {
                                        "notifications": [
                                          {
                                            "id": 45,
                                            "userId": 1,
                                            "notificationType": "CAMPAIGN_APPROVED",
                                            "title": "새 캠페인 승인",
                                            "message": "신규 캠페인이 승인되었습니다.",
                                            "relatedEntityId": 126,
                                            "relatedEntityType": "CAMPAIGN",
                                            "isRead": false,
                                            "createdAt": "2024-07-31T16:00:00",
                                            "readAt": null
                                          },
                                          {
                                            "id": 42,
                                            "userId": 1,
                                            "notificationType": "SYSTEM_NOTICE",
                                            "title": "중요 공지사항",
                                            "message": "서비스 정책 변경 안내입니다.",
                                            "relatedEntityId": null,
                                            "relatedEntityType": "SYSTEM",
                                            "isRead": false,
                                            "createdAt": "2024-07-31T15:30:00",
                                            "readAt": null
                                          }
                                        ],
                                        "unreadCount": 2
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @Parameter(
                    description = "조회할 사용자 ID", 
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    ) {
        try {
            log.info("읽지 않은 알림 조회: userId={}", userId);

            List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(userId);
            Long unreadCount = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> responseData = Map.of(
                    "notifications", unreadNotifications,
                    "unreadCount", unreadCount
            );

            return ResponseEntity.ok(
                    BaseResponse.success(responseData, "읽지 않은 알림을 성공적으로 조회했습니다.")
            );
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("읽지 않은 알림 조회에 실패했습니다.", "UNREAD_NOTIFICATION_FETCH_FAILED", 500));
        }
    }

    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = """
            특정 사용자의 읽지 않은 알림 개수만 조회합니다.
            
            ### 경로 파라미터
            - **userId**: 조회할 사용자의 ID
            
            ### 응답 정보
            - **unreadCount**: 읽지 않은 알림 개수 (숫자)
            
            ### 사용 목적
            - 앱/웹 상단 알림 배지 표시용
            - 빠른 개수 확인용 (알림 목록 불필요시)
            - 실시간 알림 개수 업데이트용
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "읽지 않은 알림 개수 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "읽지 않은 알림 개수를 성공적으로 조회했습니다.",
                                      "status": 200,
                                      "data": {
                                        "unreadCount": 5
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/{userId}/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount(
            @Parameter(
                    description = "조회할 사용자 ID", 
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    ) {
        try {
            log.info("읽지 않은 알림 개수 조회: userId={}", userId);

            Long unreadCount = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> responseData = Map.of("unreadCount", unreadCount);

            return ResponseEntity.ok(
                    BaseResponse.success(responseData, "읽지 않은 알림 개수를 성공적으로 조회했습니다.")
            );
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("읽지 않은 알림 개수 조회에 실패했습니다.", "UNREAD_COUNT_FETCH_FAILED", 500));
        }
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = """
            특정 알림을 읽음 상태로 변경합니다.
            
            ### 경로 파라미터
            - **notificationId**: 읽음 처리할 알림의 ID
            
            ### 쿼리 파라미터
            - **userId**: 사용자 ID (권한 확인용)
            
            ### 처리 결과
            - 해당 알림의 isRead를 true로 변경
            - readAt에 현재 시간 설정
            - 읽지 않은 알림 개수 1 감소
            
            ### 주의사항
            - 본인의 알림만 읽음 처리 가능
            - 이미 읽은 알림 재처리 시에도 성공 응답
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 읽음 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "알림을 읽음 상태로 변경했습니다.",
                                      "status": 200,
                                      "data": null
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (알림 ID 또는 사용자 ID 오류)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 알림)"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(
                    description = "읽음 처리할 알림 ID", 
                    example = "42",
                    required = true
            )
            @PathVariable Long notificationId,
            @Parameter(
                    description = "사용자 ID (권한 확인용)", 
                    example = "1",
                    required = true
            )
            @RequestParam Long userId
    ) {
        try {
            log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);

            notificationService.markAsRead(notificationId, userId);

            return ResponseEntity.ok(
                    BaseResponse.success(null, "알림을 읽음 상태로 변경했습니다.")
            );
        } catch (RuntimeException e) {
            log.warn("알림 읽음 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail(e.getMessage(), "NOTIFICATION_READ_FAILED", 400));
        } catch (Exception e) {
            log.error("알림 읽음 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("알림 읽음 처리에 실패했습니다.", "NOTIFICATION_READ_ERROR", 500));
        }
    }

    @Operation(
            summary = "모든 알림 읽음 처리",
            description = """
            특정 사용자의 모든 읽지 않은 알림을 읽음 상태로 변경합니다.
            
            ### 경로 파라미터
            - **userId**: 사용자 ID
            
            ### 처리 결과
            - 해당 사용자의 모든 읽지 않은 알림의 isRead를 true로 변경
            - 각 알림의 readAt에 현재 시간 설정
            - 읽지 않은 알림 개수가 0이 됨
            
            ### 사용 시나리오
            - "모두 읽음" 버튼 클릭 시
            - 알림 페이지 진입 시 자동 읽음 처리
            - 대량 알림 정리 시
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "모든 알림 읽음 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "모든 알림을 읽음 상태로 변경했습니다.",
                                      "status": 200,
                                      "data": null
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/users/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(
            @Parameter(
                    description = "모든 알림을 읽음 처리할 사용자 ID", 
                    example = "1",
                    required = true
            )
            @PathVariable Long userId
    ) {
        try {
            log.info("모든 알림 읽음 처리: userId={}", userId);

            notificationService.markAllAsRead(userId);

            return ResponseEntity.ok(
                    BaseResponse.success(null, "모든 알림을 읽음 상태로 변경했습니다.")
            );
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("모든 알림 읽음 처리에 실패했습니다.", "ALL_NOTIFICATION_READ_ERROR", 500));
        }
    }

    @Operation(
            summary = "알림 삭제",
            description = """
            특정 알림을 완전히 삭제합니다.
            
            ### 경로 파라미터
            - **notificationId**: 삭제할 알림의 ID
            
            ### 쿼리 파라미터
            - **userId**: 사용자 ID (권한 확인용)
            
            ### 처리 결과
            - 해당 알림이 데이터베이스에서 완전히 삭제됨
            - 삭제된 알림은 복구 불가능
            - 읽지 않은 알림이었다면 개수에서 제외
            
            ### 주의사항
            - 본인의 알림만 삭제 가능
            - 삭제 후에는 복구할 수 없음
            - 관련 엔티티(캠페인 등)는 삭제되지 않음
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "message": "알림이 성공적으로 삭제되었습니다.",
                                      "status": 200,
                                      "data": null
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (알림 ID 또는 사용자 ID 오류)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 알림)"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @Parameter(
                    description = "삭제할 알림 ID", 
                    example = "42",
                    required = true
            )
            @PathVariable Long notificationId,
            @Parameter(
                    description = "사용자 ID (권한 확인용)", 
                    example = "1",
                    required = true
            )
            @RequestParam Long userId
    ) {
        try {
            log.info("알림 삭제: notificationId={}, userId={}", notificationId, userId);

            notificationService.deleteNotification(notificationId, userId);

            return ResponseEntity.ok(
                    BaseResponse.success(null, "알림이 성공적으로 삭제되었습니다.")
            );
        } catch (RuntimeException e) {
            log.warn("알림 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail(e.getMessage(), "NOTIFICATION_DELETE_FAILED", 400));
        } catch (Exception e) {
            log.error("알림 삭제 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("알림 삭제에 실패했습니다.", "NOTIFICATION_DELETE_ERROR", 500));
        }
    }
}
