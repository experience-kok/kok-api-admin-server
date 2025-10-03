package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.*;
import com.example.adminservice.service.CampaignApprovalService;
import com.example.adminservice.service.CampaignApplicantService;
import com.example.adminservice.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 캠페인 승인 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Tag(name = "캠페인 관리", description = "관리자가 캠페인을 관리하는 API")
public class CampaignApprovalController {

    private final CampaignApprovalService campaignApprovalService;
    private final CampaignApplicantService campaignApplicantService;
    private final CampaignService campaignService;

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
                                        },
                                        "category": {
                                          "type": "방문",
                                          "name": "카페"
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

    @Operation(
            summary = "캠페인 신청자 목록 조회",
            description = """
                    특정 캠페인의 신청자 목록을 조회합니다.
                    
                    ### 응답 정보
                    - **ID**: 신청자 사용자 ID
                    - **닉네임**: 신청자 닉네임
                    - **이메일**: 신청자 이메일
                    - **권한**: 사용자 권한 (USER, CLIENT, ADMIN)
                    - **계정 상태**: 활성화/비활성화 상태 (true/false)
                    - **신청일**: 캠페인 신청 일시
                    - **신청 상태**: 신청 처리 상태 및 한글 텍스트
                    
                    ### 필터링 옵션
                    - **status**: 신청 상태로 필터링 (선택사항)
                      - `신청` 또는 `APPLIED`: 신청 상태
                      - `선정 대기중` 또는 `PENDING`: 선정 대기 상태
                      - `선정` 또는 `SELECTED`: 선정된 상태
                      - `거절` 또는 `REJECTED`: 거절된 상태
                      - `완료` 또는 `COMPLETED`: 완료된 상태
                      - 미입력 시: 모든 신청자 조회
                    
                    ### 페이징 기능
                    - 기본 10개씩 페이징
                    - 최신 신청자 순으로 정렬
                    - 전체 신청자 수 포함
                    
                    
                    ### 권한
                    - ADMIN 권한 필요
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(
            responseCode = "200",
            description = "캠페인 신청자 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CampaignApplicantListResponse.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": true,
                                      "message": "캠페인 신청자 목록 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "campaignId": 123,
                                        "campaignTitle": "인스타 감성 카페 체험단 모집",
                                        "totalApplicants": 25,
                                        "applicants": [
                                          {
                                            "id": 1,
                                            "nickname": "커피러버",
                                            "email": "coffee@example.com",
                                            "appliedAt": "2025-07-20T14:30:00+09:00",
                                            "applicationStatus": "SELECTED",
                                            "statusText": "선정"
                                          },
                                          {
                                            "id": 2,
                                            "nickname": "카페매니아",
                                            "email": "cafemania@example.com",
                                            "appliedAt": "2025-07-20T11:15:00+09:00",
                                            "applicationStatus": "PENDING",
                                            "statusText": "선정 대기중"
                                          },
                                          {
                                            "id": 3,
                                            "nickname": "인스타그래머",
                                            "email": "insta@example.com",
                                            "appliedAt": "2025-07-19T16:45:00+09:00",
                                            "applicationStatus": "APPLIED",
                                            "statusText": "신청"
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
    @ApiResponse(
            responseCode = "404",
            description = "캠페인을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": false,
                                      "message": "캠페인을 찾을 수 없습니다: 999",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
                                    }
                                    """
                    )
            )
    )

    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": false,
                                      "message": "유효하지 않은 신청 상태입니다: INVALID_STATUS",
                                      "errorCode": "INVALID_PARAMETER",
                                      "status": 400
                                    }
                                    """
                    )
            )
    )

    @GetMapping("/{campaignId}/applicants")
    public ResponseEntity<?> getCampaignApplicants(
            @Parameter(description = "캠페인 ID", required = true)
            @PathVariable Long campaignId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "신청 상태 필터 (신청/선정 대기중/선정/거절/완료)", example = "선정")
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

            CampaignApplicantListResponse result = campaignApplicantService.getCampaignApplicants(
                    campaignId, page, size, status
            );

            return ResponseEntity.ok(BaseResponse.success(result, "캠페인 신청자 목록 조회 성공"));

        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("캠페인을 찾을 수 없습니다")) {
                return ResponseEntity.ok(BaseResponse.fail(message, "NOT_FOUND", 404));
            } else if (message.contains("유효하지 않은")) {
                return ResponseEntity.ok(BaseResponse.fail(message, "INVALID_PARAMETER", 400));
            } else {
                log.error("캠페인 신청자 목록 조회 중 오류 발생: campaignId={}, error={}", campaignId, message, e);
                return ResponseEntity.ok(BaseResponse.fail(
                        "캠페인 신청자 목록 조회 실패: " + message,
                        "INTERNAL_ERROR",
                        500
                ));
            }
        } catch (Exception e) {
            log.error("캠페인 신청자 목록 조회 중 예기치 않은 오류 발생: campaignId={}", campaignId, e);
            return ResponseEntity.ok(BaseResponse.fail(
                    "캠페인 신청자 목록 조회 실패: 서버 내부 오류",
                    "INTERNAL_ERROR",
                    500
            ));
        }
    }
    /**
     * 캠페인 목록 조회 (ID, Title 포함)
     *
     * @return 캠페인 목록
     */
    @GetMapping("/list")
    @Operation(
            summary = "캠페인 목록 조회",
            description = "모든 캠페인의 ID와 제목을 포함한 간단한 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캠페인 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShortCampaignResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                {
                                  "success": true,
                                  "message": "캠페인 목록을 성공적으로 조회했습니다.",
                                  "status": 200,
                                  "data": [
                                    {
                                      "id": 1,
                                      "title": "신제품 운동화 리뷰 캠페인"
                                    },
                                    {
                                      "id": 2,
                                      "title": "화장품 방문형 체험 캠페인"
                                    }
                                  ]
                                }
                                """
                            )
                    )
            )
    })
    public ResponseEntity<BaseResponse.Success<List<ShortCampaignResponse>>> getCampaigns() {
        log.info("캠페인 목록 조회 API 호출");

        List<ShortCampaignResponse> campaigns = campaignService.getAllCampaigns();

        return ResponseEntity.ok(
                BaseResponse.success(campaigns, "캠페인 목록을 성공적으로 조회했습니다.")
        );
    }

}