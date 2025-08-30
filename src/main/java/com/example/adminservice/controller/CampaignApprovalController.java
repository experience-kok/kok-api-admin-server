package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.CampaignApprovalRequest;
import com.example.adminservice.dto.CampaignApprovalResponse;
import com.example.adminservice.dto.PendingCampaignResponse;
import com.example.adminservice.dto.SimpleCampaignResponse;
import com.example.adminservice.service.CampaignApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자용 캠페인 승인 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Tag(name = "캠페인 승인 관리", description = "관리자가 캠페인을 승인/거절하는 API")
public class CampaignApprovalController {

    private final CampaignApprovalService campaignApprovalService;

    @Operation(
            summary = "승인 대기 캠페인 목록 조회",
            description = "관리자가 승인 대기 중인 캠페인 목록을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "승인 대기 캠페인 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "승인 대기 캠페인 목록 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 123,
                                            "title": "인스타 감성 카페 체험단 모집",
                                            "campaignType": "인스타그램",
                                            "thumbnailUrl": "https://example.com/images/cafe.jpg",
                                            "productShortInfo": "시그니처 음료 2잔 + 디저트 1개 무료 제공",
                                            "maxApplicants": 10,
                                            "recruitmentStartDate": "2025-08-01",
                                            "recruitmentEndDate": "2025-08-15",
                                            "applicationDeadlineDate": "2025-08-14",
                                            "approvalStatus": "대기중",
                                            "approvalComment": null,
                                            "approvalDate": null
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "totalPages": 5,
                                          "totalElements": 50,
                                          "first": true,
                                          "last": false
                                        }
                                      }
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCampaigns(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        try {
            Page<PendingCampaignResponse> campaigns = campaignApprovalService.getPendingCampaigns(page, size);

            return ResponseEntity.ok(BaseResponse.successPaged(
                    campaigns.getContent(),
                    "승인 대기 캠페인 목록 조회 성공",
                    campaigns
            ));

        } catch (Exception e) {
            log.error("승인 대기 캠페인 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "승인 대기 캠페인 목록 조회 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }

    @Operation(
            summary = "전체 캠페인 목록 조회 (관리자용)",
            description = """
                    관리자가 모든 캠페인 목록을 조회합니다. 승인 상태별 필터링이 가능합니다.
                    
                    ### 필터링 옵션
                    - **approvalStatus**: 승인 상태로 필터링 (선택사항)
                      - `PENDING`: 승인 대기 캠페인만 조회
                      - `APPROVED`: 승인된 캠페인만 조회  
                      - `REJECTED`: 거절된 캠페인만 조회
                      - `EXPIRED`: 신청 마감일(applicationDeadlineDate)이 지난 만료된 캠페인만 조회
                      - 미입력 시: 모든 캠페인 조회
                    
                    ### 사용 예시
                    - `GET /campaigns` - 모든 캠페인 조회
                    - `GET /campaigns?approvalStatus=APPROVED` - 승인된 캠페인만 조회
                    - `GET /campaigns?approvalStatus=EXPIRED` - 만료된 캠페인만 조회
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 목록 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 125,
                                            "title": "운동화 리뷰 캠페인",
                                            "campaignType": "유튜브",
                                            "thumbnailUrl": "https://example.com/images/shoes.jpg",
                                            "productShortInfo": "신제품 운동화 체험 및 리뷰",
                                            "maxApplicants": 15,
                                            "recruitmentStartDate": "2025-08-01",
                                            "recruitmentEndDate": "2025-08-15",
                                            "applicationDeadlineDate": "2025-08-14",
                                            "approvalStatus": "승인됨",
                                            "approvalComment": "제품 정보가 상세하고 일정이 적절하여 승인합니다",
                                            "approvalDate": "2025-07-14T14:20:00",
                                          },
                                          {
                                            "id": 126,
                                            "title": "만료된 캠페인 예시",
                                            "campaignType": "인스타그램",
                                            "thumbnailUrl": "https://example.com/images/expired.jpg",
                                            "productShortInfo": "신청 마감일이 지난 캠페인",
                                            "maxApplicants": 8,
                                            "recruitmentStartDate": "2025-07-01",
                                            "recruitmentEndDate": "2025-07-10",
                                            "applicationDeadlineDate": "2025-07-12",
                                            "approvalStatus": "만료됨"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "totalPages": 3,
                                          "totalElements": 25,
                                          "first": true,
                                          "last": false
                                        }
                                      }
                                    }
                                    """
                    )
            )
    )
    @GetMapping
    public ResponseEntity<?> getAllCampaigns(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "승인 상태 필터 (대기중, 승인됨, 거절됨, 만료됨)",
                    example = "승인됨",
                    schema = @Schema(allowableValues = {"대기중", "승인됨", "거절됨", "만료됨"}))
            @RequestParam(required = false) String approvalStatus
    ) {
        try {
            Page<PendingCampaignResponse> campaigns = campaignApprovalService.getAllCampaigns(page, size, approvalStatus);

            return ResponseEntity.ok(BaseResponse.successPaged(
                    campaigns.getContent(),
                    "캠페인 목록 조회 성공",
                    campaigns
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.fail(
                    e.getMessage(),
                    "INVALID_PARAMETER",
                    400
            ));
        } catch (Exception e) {
            log.error("캠페인 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 목록 조회 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }

    @Operation(
            summary = "캠페인 승인/거절 처리",
            description = "관리자가 특정 캠페인을 승인하거나 거절합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 승인/거절 처리 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 승인 처리 완료",
                                      "status": 200,
                                      "data": {
                                        "campaignId": 123,
                                        "title": "인스타 감성 카페 체험단 모집",
                                        "approvalStatus": "승인됨",
                                        "approvalComment": "모든 조건을 만족하여 승인합니다.",
                                        "approvalDate": "2025-07-14T15:30:00",
                                        "approvedBy": 1,
                                        "approverName": "김관리자"
                                      }
                                    }
                                    """
                    )
            )
    )
    @PutMapping("/{campaignId}/approval")
    public ResponseEntity<?> approveCampaign(
            @Parameter(description = "캠페인 ID", required = true)
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignApprovalRequest request,
            java.security.Principal principal
    ) {
        try {
            // JWT 토큰에서 관리자 이메일 추출
            if (principal == null) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "인증된 관리자가 없습니다",
                        "UNAUTHORIZED",
                        401
                ));
            }

            String adminEmail = principal.getName();
            CampaignApprovalResponse response = campaignApprovalService.approveCampaign(adminEmail, campaignId, request);

            String message = "APPROVED".equals(request.getApprovalStatus()) ?
                    "캠페인 승인 처리 완료" : "캠페인 거절 처리 완료";

            return ResponseEntity.ok(BaseResponse.success(response, message));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.fail(
                    e.getMessage(),
                    "INVALID_PARAMETER",
                    400
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.fail(
                    e.getMessage(),
                    "CAMPAIGN_ALREADY_PROCESSED",
                    409
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.ok(BaseResponse.fail(
                        e.getMessage(),
                        "NOT_FOUND",
                        404
                ));
            } else if (e.getMessage().contains("권한")) {
                return ResponseEntity.ok(BaseResponse.fail(
                        e.getMessage(),
                        "FORBIDDEN",
                        403
                ));
            }
            throw e;
        } catch (Exception e) {
            log.error("캠페인 승인/거절 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 승인/거절 처리 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }

    @Operation(
            summary = "캠페인 상세 조회 (관리자용)",
            description = """
                    관리자가 특정 캠페인의 상세 정보를 조회합니다.
                    
                    ### 위치 정보 제공 조건
                    - 방문형 캠페인: 위치 정보 포함
                    - 배송형 캠페인: 위치 정보 제외 (location 필드가 null)
                    
                    ### 승인자 정보
                    - 승인/거절된 캠페인: approver 필드에 처리한 관리자 정보 포함
                    - 대기중 캠페인: approver 필드가 null
                    
                    ### 미션 정보 포함
                    - titleKeywords: 제목 키워드
                    - bodyKeywords: 본문 키워드 
                    - numberOfVideo: 영상 개수
                    - numberOfImage: 이미지 개수
                    - numberOfText: 글자 수
                    - isMap: 지도 포함 여부
                    - missionGuide: 미션 가이드
                    - missionStartDate: 미션 시작일
                    - missionDeadlineDate: 미션 종료일
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 상세 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "id": 123,
                                        "title": "인스타 감성 카페 체험단 모집",
                                        "campaignType": "인스타그램",
                                        "thumbnailUrl": "https://example.com/images/cafe.jpg",
                                        "productShortInfo": "시그니처 음료 2잔 + 디저트 1개 무료 제공",
                                        "maxApplicants": 10,
                                        "recruitmentStartDate": "2025-08-01",
                                        "recruitmentEndDate": "2025-08-15",
                                        "applicationDeadlineDate": "2025-08-14",
                                        "selectionDate": "2025-08-16",
                                        "reviewDeadlineDate": "2025-08-30",
                                        "approvalStatus": "승인됨",
                                        "approvalComment": "모든 조건을 만족하여 승인합니다",
                                        "approvalDate": "2025-07-14T15:30:00",
                                        "approver": {
                                          "id": 2,
                                          "nickname": "김관리자",
                                          "email": "admin@example.com"
                                        },
                                        "creator": {
                                          "id": 1,
                                          "nickname": "김클라이언트",
                                          "email": "client@example.com",
                                          "accountType": "SOCIAL",
                                          "role": "CLIENT"
                                        },
                                        "company": {
                                          "id": 1,
                                          "companyName": "맛있는 카페",
                                          "businessRegistrationNumber": "123-45-67890",
                                          "contactPerson": "김담당",
                                          "phoneNumber": "010-1234-5678"
                                        },
                                        "location": {
                                          "id": 1,
                                          "latitude": 37.5665,
                                          "longitude": 126.9780,
                                          "businessAddress": "서울특별시 강남구 테헤란로 123",
                                          "businessDetailAddress": "A동 1층 101호",
                                          "homepage": "https://cafe.example.com",
                                          "contactPhone": "02-123-4567",
                                          "visitAndReservationInfo": "평일 10:00-22:00 운영, 예약 필수",
                                          "hasCoordinates": true
                                        },
                                        "missionInfo": {
                                          "id": 1,
                                          "titleKeywords": ["맛집", "인스타그램"],
                                          "bodyKeywords": ["체험", "리뷰", "추천"],
                                          "numberOfVideo": 1,
                                          "numberOfImage": 5,
                                          "numberOfText": 300,
                                          "isMap": true,
                                          "missionGuide": "카페 방문 후 인스타그램에 리뷰 포스팅해주세요",
                                          "missionStartDate": "2025-08-17",
                                          "missionDeadlineDate": "2025-08-30",
                                          "createdAt": "2025-07-14T15:30:00",
                                          "updatedAt": "2025-07-14T15:30:00"
                                        }
                                      }
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/{campaignId}")
    public ResponseEntity<?> getCampaignDetail(
            @Parameter(description = "캠페인 ID", required = true)
            @PathVariable Long campaignId
    ) {
        try {
            PendingCampaignResponse campaign = campaignApprovalService.getCampaignDetail(campaignId);

            return ResponseEntity.ok(BaseResponse.success(campaign, "캠페인 상세 조회 성공"));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.ok(BaseResponse.fail(
                        e.getMessage(),
                        "NOT_FOUND",
                        404
                ));
            }
            throw e;
        } catch (Exception e) {
            log.error("캠페인 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 상세 조회 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }


    @Operation(
            summary = "캠페인 검색",
            description = """
                    관리자가 캠페인을 검색합니다. 제목, 내용, 생성자 등으로 검색 가능합니다.
                    
                    ### 검색 기능
                    - **keyword**: 검색할 키워드 (필수)
                      - 캠페인 제목에서 검색
                      - 제품 정보에서 검색
                      - 생성자 닉네임에서 검색
                      - 회사명에서 검색
                    - **approvalStatus**: 승인 상태로 추가 필터링 (선택사항)
                    
                    ### 사용 예시
                    - `GET /campaigns/search?keyword=카페` - '카페'가 포함된 캠페인 검색
                    - `GET /campaigns/search?keyword=인스타&approvalStatus=승인됨` - '인스타'가 포함된 승인된 캠페인만 검색
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 검색 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 검색 성공",
                                      "status": 200,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 125,
                                            "title": "운동화 리뷰 캠페인",
                                            "campaignType": "유튜브",
                                            "thumbnailUrl": "https://example.com/images/shoes.jpg",
                                            "productShortInfo": "신제품 운동화 체험 및 리뷰",
                                            "maxApplicants": 15,
                                            "recruitmentStartDate": "2025-08-01",
                                            "recruitmentEndDate": "2025-08-15",
                                            "selectionDate": "2025-08-16",
                                            "approvalStatus": "승인됨",
                                            "approvalComment": "제품 정보가 상세하고 일정이 적절하여 승인합니다",
                                            "approvalDate": "2025-07-14T14:20:00",
                                            "approver": {
                                              "id": 2,
                                              "nickname": "김관리자",
                                              "email": "admin@example.com"
                                            }
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "totalPages": 1,
                                          "totalElements": 5,
                                          "first": true,
                                          "last": true
                                        }
                                      }
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/search")
    public ResponseEntity<?> searchCampaigns(
            @Parameter(description = "검색 키워드", required = true, example = "카페")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "승인 상태 필터 (대기중, 승인됨, 거절됨, 만료됨)",
                    example = "승인됨")
            @RequestParam(required = false) String approvalStatus
    ) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "검색 키워드는 필수입니다",
                        "INVALID_PARAMETER",
                        400
                ));
            }

            Page<SimpleCampaignResponse> campaigns = campaignApprovalService.searchCampaigns(
                    keyword.trim(), page, size, approvalStatus);

            return ResponseEntity.ok(BaseResponse.successPaged(
                    campaigns.getContent(),
                    "캠페인 검색 성공",
                    campaigns
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.fail(
                    e.getMessage(),
                    "INVALID_PARAMETER",
                    400
            ));
        } catch (Exception e) {
            log.error("캠페인 검색 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 검색 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }

    @Operation(
            summary = "캠페인 삭제 (관리자용)",
            description = """
                    관리자가 특정 캠페인을 삭제합니다.
                    
                    ### 주의사항
                    - 삭제된 캠페인은 복구할 수 없습니다
                    - 연관된 모든 데이터(신청, 위치, 미션 정보 등)도 함께 삭제됩니다
                    - 승인된 캠페인 삭제 시 신중하게 결정해야 합니다
                    - 캠페인 삭제 시 생성자에게 알림이 전송됩니다
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인이 성공적으로 삭제되었습니다",
                                      "status": 200,
                                      "data": {
                                        "deletedCampaignId": 123,
                                        "deletedAt": "2025-08-19T15:30:00",
                                        "deletedBy": "admin@example.com"
                                      }
                                    }
                                    """
                    )
            )
    )
    @DeleteMapping("/{campaignId}")
    public ResponseEntity<?> deleteCampaign(
            @Parameter(description = "삭제할 캠페인 ID", required = true)
            @PathVariable Long campaignId,
            java.security.Principal principal
    ) {
        try {
            // JWT 토큰에서 관리자 이메일 추출
            if (principal == null) {
                return ResponseEntity.ok(BaseResponse.fail(
                        "인증된 관리자가 없습니다",
                        "UNAUTHORIZED",
                        401
                ));
            }

            String adminEmail = principal.getName();
            campaignApprovalService.deleteCampaign(adminEmail, campaignId);

            // 삭제 완료 응답 데이터
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deletedCampaignId", campaignId);
            responseData.put("deletedAt", java.time.LocalDateTime.now());
            responseData.put("deletedBy", adminEmail);

            return ResponseEntity.ok(BaseResponse.success(
                    responseData,
                    "캠페인이 성공적으로 삭제되었습니다"
            ));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.ok(BaseResponse.fail(
                        e.getMessage(),
                        "NOT_FOUND",
                        404
                ));
            } else if (e.getMessage().contains("권한")) {
                return ResponseEntity.ok(BaseResponse.fail(
                        e.getMessage(),
                        "FORBIDDEN",
                        403
                ));
            }
            throw e;
        } catch (Exception e) {
            log.error("캠페인 삭제 중 오류 발생: campaignId={}, error={}", campaignId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 삭제 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }

    @Operation(
            summary = "캠페인 통계 정보 조회",
            description = """
                    관리자가 캠페인 관련 통계 정보를 조회합니다.
                    
                    ### 제공 통계
                    - 전체 캠페인 수
                    - 승인 대기 캠페인 수
                    - 승인된 캠페인 수
                    - 거절된 캠페인 수
                    - 만료된 캠페인 수 (신청 마감일이 지난 캠페인)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 통계 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 통계 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "totalCampaigns": 150,
                                        "pendingCampaigns": 25,
                                        "approvedCampaigns": 100,
                                        "rejectedCampaigns": 25,
                                        "expiredCampaigns": 12
                                      }
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/stats")
    public ResponseEntity<?> getCampaignStats() {
        try {
            Map<String, Object> stats = campaignApprovalService.getCampaignStats();

            return ResponseEntity.ok(BaseResponse.success(stats, "캠페인 통계 조회 성공"));

        } catch (Exception e) {
            log.error("캠페인 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 통계 조회 실패: " + e.getMessage(),
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }
}