package com.example.adminservice.controller;

import com.example.adminservice.dto.BannerImageRequest;
import com.example.adminservice.dto.BannerImageResponse;
import com.example.adminservice.dto.BannerOrderUpdateRequest;
import com.example.adminservice.service.BannerImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "배너 이미지 관리 API", description = "관리자용 배너 이미지 생성, 수정, 삭제, 조회 API")
public class BannerImageController {

    private final BannerImageService bannerImageService;

    @Operation(summary = "새 배너 이미지 생성", description = "새로운 배너 이미지를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배너 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너가 성공적으로 생성되었습니다",
                                      "status": 201,
                                      "data": {
                                        "id": 1,
                                        "title": "신규 캠페인 프로모션",
                                        "description": "새로운 캠페인을 확인해보세요!",
                                        "bannerUrl": "https://example.com/banner.jpg",
                                        "redirectUrl": "https://example.com/campaign",
                                        "position": "TOP",
                                        "displayOrder": 1,
                                        "createdAt": "2025-08-06T10:30:00",
                                        "updatedAt": "2025-08-06T10:30:00"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 이미지 URL은 필수입니다",
                                      "errorCode": "VALIDATION_ERROR",
                                      "status": 400
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 생성 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBanner(@RequestBody @Valid BannerImageRequest request) {
        try {
            log.info("배너 생성 요청: bannerUrl={}, title={}", request.getBannerUrl(), request.getTitle());

            if (request.getBannerUrl() != null && request.getBannerUrl().contains("?")) {
                String cleanUrl = request.getBannerUrl().split("\\?")[0];
                request.setBannerUrl(cleanUrl);
            }

            BannerImageResponse response = bannerImageService.createBanner(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "배너가 성공적으로 생성되었습니다",
                            "data", response,
                            "status", HttpStatus.CREATED.value()
                    ));
        } catch (Exception e) {
            log.error("배너 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 생성 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Operation(summary = "배너 이미지 수정", description = "기존 배너 이미지를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너가 성공적으로 수정되었습니다",
                                      "status": 200,
                                      "data": {
                                        "id": 1,
                                        "title": "수정된 배너 제목",
                                        "description": "수정된 배너 설명",
                                        "bannerUrl": "https://example.com/updated_banner.jpg",
                                        "redirectUrl": "https://example.com/new_redirect",
                                        "position": "MIDDLE",
                                        "displayOrder": 2,
                                        "createdAt": "2025-08-06T10:30:00",
                                        "updatedAt": "2025-08-06T11:45:00"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너를 찾을 수 없습니다",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 수정 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBanner(
            @Parameter(description = "배너 ID", required = true) @PathVariable Long id,
            @RequestBody @Valid BannerImageRequest request) {
        try {
            log.info("배너 수정 요청: id={}, title={}", id, request.getTitle());

            BannerImageResponse response = bannerImageService.updateBanner(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "배너가 성공적으로 수정되었습니다",
                    "data", response,
                    "status", HttpStatus.OK.value()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "배너를 찾을 수 없습니다",
                                "errorCode", "NOT_FOUND",
                                "status", HttpStatus.NOT_FOUND.value()
                        ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 수정 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Operation(summary = "배너 이미지 삭제", description = "지정된 ID의 배너 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너가 성공적으로 삭제되었습니다",
                                      "status": 200
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너를 찾을 수 없습니다",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 삭제 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBanner(@Parameter(description = "삭제할 배너 ID") @PathVariable Long id) {
        try {
            log.info("배너 삭제 요청: id={}", id);
            bannerImageService.deleteBanner(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "배너가 성공적으로 삭제되었습니다",
                    "status", HttpStatus.OK.value()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "배너를 찾을 수 없습니다",
                                "errorCode", "NOT_FOUND",
                                "status", HttpStatus.NOT_FOUND.value()
                        ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 삭제 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Operation(summary = "배너 이미지 상세 조회", description = "지정된 ID의 배너 이미지 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너 조회 성공",
                                      "status": 200,
                                      "data": {
                                        "id": 1,
                                        "title": "메인 배너",
                                        "description": "메인 페이지 배너입니다",
                                        "bannerUrl": "https://example.com/banner.jpg",
                                        "redirectUrl": "https://example.com/page",
                                        "position": "TOP",
                                        "displayOrder": 1,
                                        "createdAt": "2025-08-06T10:30:00",
                                        "updatedAt": "2025-08-06T10:30:00"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너를 찾을 수 없습니다",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 조회 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBanner(@Parameter(description = "조회할 배너 ID") @PathVariable Long id) {
        try {
            log.info("배너 조회 요청: id={}", id);
            BannerImageResponse response = bannerImageService.getBanner(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "배너 조회 성공",
                    "data", response,
                    "status", HttpStatus.OK.value()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "배너를 찾을 수 없습니다",
                                "errorCode", "NOT_FOUND",
                                "status", HttpStatus.NOT_FOUND.value()
                        ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 조회 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Operation(summary = "모든 배너 이미지 목록 조회",
            description = "등록된 모든 배너 이미지 목록을 조회합니다. orderBy 파라미터로 정렬 방식을 선택할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너 목록 조회 성공",
                                      "status": 200,
                                      "data": [
                                        {
                                          "id": 1,
                                          "title": "첫 번째 배너",
                                          "description": "첫 번째 배너 설명",
                                          "bannerUrl": "https://example.com/banner1.jpg",
                                          "redirectUrl": "https://example.com/page1",
                                          "position": "TOP",
                                          "displayOrder": 1,
                                          "createdAt": "2025-08-06T10:30:00",
                                          "updatedAt": "2025-08-06T10:30:00"
                                        },
                                        {
                                          "id": 2,
                                          "title": "두 번째 배너",
                                          "description": "두 번째 배너 설명",
                                          "bannerUrl": "https://example.com/banner2.jpg",
                                          "redirectUrl": "https://example.com/page2",
                                          "position": "MIDDLE",
                                          "displayOrder": 2,
                                          "createdAt": "2025-08-06T11:00:00",
                                          "updatedAt": "2025-08-06T11:00:00"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 목록 조회 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBanners(
            @Parameter(description = "정렬 방식 (order: 표시순서, created: 생성일순, id: ID순)")
            @RequestParam(defaultValue = "order") String orderBy) {
        try {
            log.info("배너 목록 조회 요청: orderBy={}", orderBy);

            List<BannerImageResponse> responses = switch (orderBy.toLowerCase()) {
                case "id" -> bannerImageService.getAllBannersOrderById();
                case "created" -> bannerImageService.getAllBanners();
                default -> bannerImageService.getAllBannersOrderByDisplayOrder();
            };

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "배너 목록 조회 성공",
                    "data", responses,
                    "status", HttpStatus.OK.value()
            ));
        } catch (Exception e) {
            log.error("배너 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 목록 조회 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Operation(summary = "배너 표시 순서 일괄 변경",
            description = "여러 배너의 표시 순서를 한 번에 변경합니다. 드래그&드롭으로 변경된 순서를 전송하세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "순서 변경 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "배너 순서가 성공적으로 변경되었습니다",
                                      "status": 200,
                                      "data": [
                                        {
                                          "id": 2,
                                          "title": "두 번째 배너",
                                          "description": "이제 첫 번째로 표시됩니다",
                                          "bannerUrl": "https://example.com/banner2.jpg",
                                          "redirectUrl": "https://example.com/page2",
                                          "position": "TOP",
                                          "displayOrder": 1,
                                          "createdAt": "2025-08-06T11:00:00",
                                          "updatedAt": "2025-08-06T12:00:00"
                                        },
                                        {
                                          "id": 1,
                                          "title": "첫 번째 배너",
                                          "description": "이제 두 번째로 표시됩니다",
                                          "bannerUrl": "https://example.com/banner1.jpg",
                                          "redirectUrl": "https://example.com/page1",
                                          "position": "TOP",
                                          "displayOrder": 2,
                                          "createdAt": "2025-08-06T10:30:00",
                                          "updatedAt": "2025-08-06T12:00:00"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "순서 변경 요청이 올바르지 않습니다",
                                      "errorCode": "BAD_REQUEST",
                                      "status": 400
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너를 찾을 수 없습니다",
                                      "errorCode": "NOT_FOUND",
                                      "status": 404
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "배너 순서 변경 중 오류가 발생했습니다",
                                      "errorCode": "INTERNAL_ERROR",
                                      "status": 500
                                    }
                                    """)))
    })
    @PatchMapping("/order")
    public ResponseEntity<Map<String, Object>> updateBannerOrders(
            @Parameter(description = "배너 순서 변경 요청",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "banners": [
                                {"id": 2, "displayOrder": 1},
                                {"id": 1, "displayOrder": 2},
                                {"id": 3, "displayOrder": 3}
                              ]
                            }
                            """)))
            @RequestBody @Valid BannerOrderUpdateRequest request) {
        try {
            log.info("배너 순서 변경 요청: {} 개 배너", request.getBanners().size());

            List<BannerImageResponse> responses = bannerImageService.updateBannerOrders(request.getBanners());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "배너 순서가 성공적으로 변경되었습니다",
                    "data", responses,
                    "status", HttpStatus.OK.value()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "배너를 찾을 수 없습니다",
                                "errorCode", "NOT_FOUND",
                                "status", HttpStatus.NOT_FOUND.value()
                        ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", "순서 변경 요청이 올바르지 않습니다",
                            "errorCode", "BAD_REQUEST",
                            "status", HttpStatus.BAD_REQUEST.value()
                    ));
        } catch (Exception e) {
            log.error("배너 순서 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "배너 순서 변경 중 오류가 발생했습니다",
                            "errorCode", "INTERNAL_ERROR",
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }
}
