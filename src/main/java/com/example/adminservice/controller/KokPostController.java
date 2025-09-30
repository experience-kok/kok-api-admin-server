package com.example.adminservice.controller;

import com.example.adminservice.common.ApiResponse;
import com.example.adminservice.constant.SortOption;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.*;
import com.example.adminservice.service.KokPostService;
import com.example.adminservice.util.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "체험콕 아티클 API", description = "체험콕 아티클 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class KokPostController {

    private final KokPostService kokPostService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "체험콕 아티클 전체 목록 조회 (페이지네이션)",
            description = "모든 체험콕 아티클 목록을 페이지네이션으로 조회합니다.\n\n" +
                    "정렬 옵션:\n" +
                    "- latest: 최신순 (기본값)\n" +
                    "페이지네이션 파라미터:\n" +
                    "- page: 페이지 번호 (0부터 시작, 기본값: 0)\n" +
                    "- size: 페이지 크기 (기본값: 10)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 아티클 목록을 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 1,
                                                    "title": "맛집 체험 후기",
                                                    "viewCount": 156,
                                                    "campaignId": 1,
                                                    "authorId": 1,
                                                    "authorName": "관리자",
                                                    "active": true,
                                                    "visitInfo": {
                                                      "contactPhone": "02-1234-5678",
                                                      "homepage": "https://example.com",
                                                      "businessAddress": "서울시 강남구 테헤란로",
                                                      "businessDetailAddress": "123-45 건물 2층",
                                                      "lat": 37.5665,
                                                      "lng": 126.9780
                                                    },
                                                    "createdAt": "2025-08-26T10:30:00",
                                                    "updatedAt": "2025-08-26T15:45:00"
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
                                            }"""
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<PagedResponse<KokPostListResponse>> getAllKokPosts(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "정렬 옵션 (latest: 최신순, viewCountDesc: 조회수 높은순, viewCountAsc: 조회수 낮은순)")
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @Parameter(description = "활성 상태 필터 (true: 활성화된 글만, false: 비활성화된 글만, null: 전체)")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        log.info("체험콕 글 전체 목록 조회 API 호출 - 정렬: {}, 활성 필터: {}, 페이지: {}, 크기: {}", 
                sort, active, page, size);

        SortOption sortOption = SortOption.fromValue(sort);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KokPostListResponse> kokPosts = kokPostService.getAllKokPosts(sortOption, pageable, active);

        return ApiResponse.success("체험콕 글 목록을 성공적으로 조회했습니다.", kokPosts);
    }

    @Operation(
            summary = "체험콕 글 생성",
            description = """
                    새로운 체험콕 글을 생성합니다.
                    
                    **중요: 활성화(active=true) 상태로 생성하려면 campaignId가 필수입니다.**
                    
                    요청 본문:
                    - title: 글 제목 (필수)
                    - content: 글 내용 (필수)
                    - active: 활성 여부 (필수, true/false)
                      - true: 활성화 - campaignId 필수, 목록에 바로 표시됨
                      - false: 비활성화 - campaignId 선택, 임시 저장 상태
                    - campaignId: 캠페인 ID (active=true일 때 필수)
                    - visitInfo: 방문 정보 (필수 객체)
                      - contactPhone: 연락처 (필수)
                      - homepage: 홈페이지 주소 (선택)
                      - businessAddress: 위치 정보 (선택)
                      - businessDetailAddress: 위치 정보 상세 (선택)
                      - lat: 위도 (선택)
                      - lng: 경도 (선택)
                    
                    요청 예시 1 (활성화 상태):
                    {
                      "title": "맛집 체험 후기",
                      "content": "정말 맛있는 곳입니다",
                      "active": true,
                      "campaignId": 123,
                      "visitInfo": {
                        "contactPhone": "02-1234-5678",
                        "businessAddress": "서울시 강남구"
                      }
                    }
                    
                    요청 예시 2 (비활성화 상태 - 임시 저장):
                    {
                      "title": "작성 중인 글",
                      "content": "내용 작성 중...",
                      "active": false,
                      "campaignId": null,
                      "visitInfo": {
                        "contactPhone": "02-1234-5678"
                      }
                    }
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "체험콕 글 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글이 성공적으로 생성되었습니다.",
                                              "status": 201,
                                              "data": {
                                                "id": 1,
                                                "title": "맛집 체험 후기",
                                                "content": "이곳은 정말 맛있는 식당입니다...",
                                                "viewCount": 0,
                                                "campaignId": 1,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "active": true,
                                                "visitInfo": {
                                                  "contactPhone": "02-1234-5678",
                                                  "homepage": "https://example.com",
                                                  "businessAddress": "서울시 강남구 테헤란로",
                                                  "businessDetailAddress": "123-45 건물 2층",
                                                  "lat": 37.5665,
                                                  "lng": 126.9780
                                                },
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T10:30:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PostMapping
    public ApiResponse<KokPostDetailResponse> createKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody KokPostCreateRequest request
    ) {
        log.info("체험콕 글 생성 API 호출 - 제목: {}", request.getTitle());

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);
        
        KokPostDetailResponse response = kokPostService.createKokPost(request, user.getId(), user.getNickname());

        return ApiResponse.success("체험콕 글 성공적으로 생성되었습니다.", response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "체험콕 글 개별 조회",
            description = "특정 ID의 체험콕 글을 상세 조회합니다. (조회수 증가)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "콕포스트를 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "맛집 체험 후기",
                                                "content": "이곳은 정말 맛있는 식당입니다...",
                                                "viewCount": 157,
                                                "campaignId": 1,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "active": true,
                                                "visitInfo": {
                                                  "contactPhone": "02-1234-5678",
                                                  "homepage": "https://example.com",
                                                  "businessAddress": "서울시 강남구 테헤란로",
                                                  "businessDetailAddress": "123-45 건물 2층",
                                                  "lat": 37.5665,
                                                  "lng": 126.9780
                                                },
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T14:20:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<KokPostDetailResponse> getKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "콕포스트 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("콕포스트 개별 조회 API 호출 - ID: {}", id);

        KokPostDetailResponse response = kokPostService.getKokPost(id);

        return ApiResponse.success("콕포스트를 성공적으로 조회했습니다.", response);
    }

    @Operation(
            summary = "체험콕 글 수정",
            description = "특정 ID의 체험콕 글 수정합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 체험콕 글 수정 가능\n" +
                    "- ADMIN: 모든 체험콕 글 수정 가능\n\n" +
                    "요청 본문 (모든 필드 선택적):\n" +
                    "- title: 수정할 제목 (선택)\n" +
                    "- content: 수정할 내용 (선택)\n" +
                    "- visitInfo: 수정할 방문 정보 (선택 객체)\n" +
                    "  - contactPhone: 연락처 (필수 - visitInfo 제공시)\n" +
                    "  - homepage: 홈페이지 주소 (선택)\n" +
                    "  - businessAddress: 위치 정보 (선택)\n" +
                    "  - businessDetailAddress: 위치 정보 상세 (선택)\n" +
                    "  - lat: 위도 (선택)\n" +
                    "  - lng: 경도 (선택)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글이 성공적으로 수정되었습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "수정된 맛집 후기",
                                                "content": "수정된 내용입니다...",
                                                "viewCount": 156,
                                                "campaignId": 1,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "active": true,
                                                "visitInfo": {
                                                  "contactPhone": "02-1234-5678",
                                                  "homepage": "https://example.com",
                                                  "businessAddress": "서울시 강남구 테헤란로",
                                                  "businessDetailAddress": "123-45 건물 2층",
                                                  "lat": 37.5665,
                                                  "lng": 126.9780
                                                },
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T15:45:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ApiResponse<KokPostDetailResponse> updateKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "체험콕 글 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody KokPostUpdateRequest request
    ) {
        log.info("체험콕 글 수정 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        KokPostDetailResponse response = kokPostService.updateKokPost(id, request, user.getId(), user.getRole());

        return ApiResponse.success("체험콕 글이 성공적으로 수정되었습니다.", response);
    }

    @Operation(
            summary = "체험콕 글 삭제",
            description = "특정 ID의 체험콕 글을 삭제합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 글만 삭제 가능\n" +
                    "- ADMIN: 모든 체험콕 글 삭제 가능"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글이 성공적으로 삭제되었습니다.",
                                              "status": 200
                                            }"""
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "체험콕 글 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("체험콕 글 삭제 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        kokPostService.deleteKokPost(id, user.getId(), user.getRole());

        return ApiResponse.success("체험콕 글이 성공적으로 삭제되었습니다.");
    }

    @Operation(
            summary = "체험콕 글 제목 검색 (페이지네이션)",
            description = "제목으로 체험콕 글을 검색합니다.\n\n" +
                    "정렬 옵션:\n" +
                    "- latest: 최신순 (기본값)\n" +
                    "- viewCountDesc: 조회수 높은순\n" +
                    "- viewCountAsc: 조회수 낮은순\n\n" +
                    "페이지네이션 파라미터:\n" +
                    "- page: 페이지 번호 (0부터 시작, 기본값: 0)\n" +
                    "- size: 페이지 크기 (기본값: 10)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글 검색을 성공적으로 완료했습니다.",
                                              "status": 200,
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 1,
                                                    "title": "맛집 체험 후기",
                                                    "viewCount": 156,
                                                    "campaignId": 1,
                                                    "authorId": 1,
                                                    "authorName": "관리자",
                                                    "active": true,
                                                    "visitInfo": {
                                                      "contactPhone": "02-1234-5678",
                                                      "homepage": "https://example.com",
                                                      "businessAddress": "서울시 강남구 테헤란로",
                                                      "businessDetailAddress": "123-45 건물 2층",
                                                      "lat": 37.5665,
                                                      "lng": 126.9780
                                                    },
                                                    "createdAt": "2025-08-27T10:30:00",
                                                    "updatedAt": "2025-08-27T15:45:00"
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
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ApiResponse<PagedResponse<KokPostListResponse>> searchKokPosts(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "검색할 제목 키워드")
            @RequestParam String title,
            @Parameter(description = "정렬 옵션 (latest: 최신순, viewCountDesc: 조회수 높은순, viewCountAsc: 조회수 낮은순)")
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @Parameter(description = "활성 상태 필터 (true: 활성화된 글만, false: 비활성화된 글만, null: 전체)")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        log.info("체험콕 글 제목 검색 API 호출 - 키워드: {}, 정렬: {}, 활성 필터: {}, 페이지: {}, 크기: {}", 
                title, sort, active, page, size);

        SortOption sortOption = SortOption.fromValue(sort);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KokPostListResponse> kokPosts = kokPostService.searchKokPostsByTitle(title, sortOption, pageable, active);

        return ApiResponse.success("체험콕 글 검색을 성공적으로 완료했습니다.", kokPosts);
    }

    @Operation(
            summary = "체험콕 글 비활성화",
            description = "특정 ID의 체험콕 글을 비활성화합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 글만 비활성화 가능\n" +
                    "- ADMIN: 모든 체험콕 글 비활성화 가능\n\n" +
                    "비활성화된 글은 목록에서 기본적으로 숨겨지지만, active=false 필터로 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비활성화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글이 성공적으로 비활성화되었습니다.",
                                              "status": 200
                                            }"""
                            )
                    )
            )
    })
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivateKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "체험콕 글 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("체험콕 글 비활성화 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        kokPostService.deactivateKokPost(id, user.getId(), user.getRole());

        return ApiResponse.success("체험콕 글이 성공적으로 비활성화되었습니다.");
    }

    @Operation(
            summary = "체험콕 글 활성화",
            description = "특정 ID의 체험콕 글을 다시 활성화합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 글만 활성화 가능\n" +
                    "- ADMIN: 모든 체험콕 글 활성화 가능\n\n" +
                    "비활성화된 글을 다시 활성화하여 목록에 표시되도록 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "활성화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "체험콕 글이 성공적으로 활성화되었습니다.",
                                              "status": 200
                                            }"""
                            )
                    )
            )
    })
    @PatchMapping("/{id}/activate")
    public ApiResponse<Void> activateKokPost(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "체험콕 글 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("체험콕 글 활성화 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        kokPostService.activateKokPost(id, user.getId(), user.getRole());

        return ApiResponse.success("체험콕 글이 성공적으로 활성화되었습니다.");
    }

}
