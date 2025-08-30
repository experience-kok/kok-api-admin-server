package com.example.adminservice.controller;

import com.example.adminservice.common.ApiResponse;
import com.example.adminservice.constant.SortOption;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.*;
import com.example.adminservice.service.NoticeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "공지사항 전체 목록 조회 (페이지네이션)",
            description = "모든 공지사항의 목록을 페이지네이션으로 조회합니다.\n\n" +
                    "정렬 옵션:\n" +
                    "- latest: 최신순 (기본값)\n"
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
                                              "message": "공지사항 목록을 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "notices": [
                                                  {
                                                    "id": 1,
                                                    "title": "중요한 공지사항입니다",
                                                    "viewCount": 156,
                                                    "isMustRead": true,
                                                    "authorId": 1,
                                                    "authorName": "관리자",
                                                    "createdAt": "2025-08-27T10:30:00",
                                                    "updatedAt": "2025-08-27T15:45:00"
                                                  }
                                                ],
                                                "currentPage": 0,
                                                "totalPages": 5,
                                                "size": 10,
                                                "totalElements": 48,
                                                "first": true,
                                                "last": false,
                                                "empty": false
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<NoticePageResponse> getAllNotices(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "정렬 옵션 (latest: 최신순, viewCountDesc: 조회수 높은순, viewCountAsc: 조회수 낮은순)")
            @RequestParam(required = false, defaultValue = "latest") String sort
    ) {
        log.info("공지사항 전체 목록 조회 API 호출 - page: {}, size: {}, 정렬: {}", page, size, sort);

        SortOption sortOption = SortOption.fromValue(sort);
        NoticePageResponse response = noticeService.getAllNotices(page, size, sortOption);

        return ApiResponse.success("공지사항 목록을 성공적으로 조회했습니다.", response);
    }

    @Operation(
            summary = "공지사항 생성",
            description = "새로운 공지사항을 생성합니다.\n\n" +
                    "요청 본문:\n" +
                    "- title: 공지사항 제목 (필수, 최대 200자)\n" +
                    "- content: 공지사항 내용 (필수)\n" +
                    "- isMustRead: 필독 여부 (선택, 기본값: false)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "공지사항 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "공지사항이 성공적으로 생성되었습니다.",
                                              "status": 201,
                                              "data": {
                                                "id": 1,
                                                "title": "중요한 공지사항입니다",
                                                "content": "공지사항 내용입니다...",
                                                "viewCount": 0,
                                                "isMustRead": true,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "createdAt": "2025-08-27T10:30:00",
                                                "updatedAt": "2025-08-27T10:30:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PostMapping
    public ApiResponse<NoticeDetailResponse> createNotice(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        log.info("공지사항 생성 API 호출 - 제목: {}", request.getTitle());

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        NoticeDetailResponse response = noticeService.createNotice(request, user.getId(), user.getNickname());

        return ApiResponse.success("공지사항이 성공적으로 생성되었습니다.", response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "공지사항 개별 조회",
            description = "특정 ID의 공지사항을 상세 조회합니다. (조회수 증가)"
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
                                              "message": "공지사항을 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "중요한 공지사항입니다",
                                                "content": "공지사항 내용입니다...",
                                                "viewCount": 157,
                                                "isMustRead": true,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "createdAt": "2025-08-27T10:30:00",
                                                "updatedAt": "2025-08-27T14:20:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<NoticeDetailResponse> getNotice(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "공지사항 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("공지사항 개별 조회 API 호출 - ID: {}", id);

        NoticeDetailResponse response = noticeService.getNotice(id);

        return ApiResponse.success("공지사항을 성공적으로 조회했습니다.", response);
    }

    @Operation(
            summary = "공지사항 수정",
            description = "특정 ID의 공지사항을 수정합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 공지사항만 수정 가능\n" +
                    "- ADMIN: 모든 공지사항 수정 가능\n\n" +
                    "요청 본문 (모든 필드 선택적):\n" +
                    "- title: 수정할 제목 (선택)\n" +
                    "- content: 수정할 내용 (선택)\n" +
                    "- isMustRead: 필독 여부 (선택)"
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
                                              "message": "공지사항이 성공적으로 수정되었습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "수정된 공지사항 제목",
                                                "content": "수정된 공지사항 내용...",
                                                "viewCount": 156,
                                                "isMustRead": false,
                                                "authorId": 1,
                                                "authorName": "관리자",
                                                "createdAt": "2025-08-27T10:30:00",
                                                "updatedAt": "2025-08-27T15:45:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ApiResponse<NoticeDetailResponse> updateNotice(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "공지사항 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        log.info("공지사항 수정 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        NoticeDetailResponse response = noticeService.updateNotice(id, request, user.getId(), user.getRole());

        return ApiResponse.success("공지사항이 성공적으로 수정되었습니다.", response);
    }

    @Operation(
            summary = "공지사항 삭제",
            description = "특정 ID의 공지사항을 삭제합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 공지사항만 삭제 가능\n" +
                    "- ADMIN: 모든 공지사항 삭제 가능"
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
                                              "message": "공지사항이 성공적으로 삭제되었습니다.",
                                              "status": 200
                                            }"""
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotice(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "공지사항 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("공지사항 삭제 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        noticeService.deleteNotice(id, user.getId(), user.getRole());

        return ApiResponse.success("공지사항이 성공적으로 삭제되었습니다.");
    }

    @Operation(
            summary = "공지사항 제목 검색 (페이지네이션)",
            description = "제목으로 공지사항을 검색합니다.\n\n" +
                    "정렬 옵션:\n" +
                    "- latest: 최신순 (기본값)\n" +
                    "- viewCountDesc: 조회수 높은순\n" +
                    "- viewCountAsc: 조회수 낮은순"
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
                                              "message": "공지사항 검색을 성공적으로 완료했습니다.",
                                              "status": 200,
                                              "data": {
                                                "notices": [
                                                  {
                                                    "id": 1,
                                                    "title": "중요한 공지사항입니다",
                                                    "viewCount": 156,
                                                    "isMustRead": true,
                                                    "authorId": 1,
                                                    "authorName": "관리자",
                                                    "createdAt": "2025-08-27T10:30:00",
                                                    "updatedAt": "2025-08-27T15:45:00"
                                                  }
                                                ],
                                                "currentPage": 0,
                                                "totalPages": 2,
                                                "size": 10,
                                                "totalElements": 15,
                                                "first": true,
                                                "last": false,
                                                "empty": false
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ApiResponse<NoticePageResponse> searchNotices(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "검색할 제목 키워드")
            @RequestParam String title,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "정렬 옵션 (latest: 최신순, viewCountDesc: 조회수 높은순, viewCountAsc: 조회수 낮은순)")
            @RequestParam(required = false, defaultValue = "latest") String sort
    ) {
        log.info("공지사항 제목 검색 API 호출 - 키워드: {}, 정렬: {}", title, sort);

        SortOption sortOption = SortOption.fromValue(sort);
        NoticePageResponse response = noticeService.searchNoticesByTitle(title, page, size, sortOption);

        return ApiResponse.success("공지사항 검색을 성공적으로 완료했습니다.", response);
    }
}
