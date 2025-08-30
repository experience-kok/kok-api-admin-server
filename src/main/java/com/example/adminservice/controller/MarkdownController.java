package com.example.adminservice.controller;

import com.example.adminservice.common.ApiResponse;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.*;
import com.example.adminservice.service.MarkdownService;
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

@Tag(name = "마크다운 API", description = "마크다운 문서 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/markdowns")
@RequiredArgsConstructor
public class MarkdownController {

    private final MarkdownService markdownService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "마크다운 전체 목록 조회",
            description = "모든 마크다운 문서의 목록을 최신순으로 조회합니다.\n\n" +
                    "응답 구조:\n" +
                    "- data 필드 안에 markdowns 배열로 반환됩니다.\n" +
                    "- 각 항목은 요약 정보만 포함 (상세 내용 제외)"
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
                                              "message": "마크다운 목록을 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "markdowns": [
                                                  {
                                                    "id": 1,
                                                    "title": "Spring Boot 완전 가이드",
                                                    "viewCount": 156,
                                                    "authorId": 1,
                                                    "authorName": "개발자",
                                                    "createdAt": "2025-08-26T10:30:00",
                                                    "updatedAt": "2025-08-26T15:45:00"
                                                  },
                                                  {
                                                    "id": 2,
                                                    "title": "Java 기초 문법",
                                                    "viewCount": 89,
                                                    "authorId": 2,
                                                    "authorName": "관리자",
                                                    "createdAt": "2025-08-25T14:20:00",
                                                    "updatedAt": "2025-08-25T14:20:00"
                                                  }
                                                ]
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<MarkdownListResponseWrapper> getAllMarkdowns(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("마크다운 전체 목록 조회 API 호출");

        List<MarkdownListResponse> markdowns = markdownService.getAllMarkdowns();
        MarkdownListResponseWrapper responseData = MarkdownListResponseWrapper.of(markdowns);

        return ApiResponse.success("마크다운 목록을 성공적으로 조회했습니다.", responseData);
    }

    @Operation(
            summary = "마크다운 생성",
            description = "새로운 마크다운 문서를 생성합니다.\n\n" +
                    "요청 본문 구조:\n" +
                    "- title: 마크다운 제목 (필수, 최대 255자)\n" +
                    "- content: 마크다운 내용 (필수)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "마크다운 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "마크다운이 성공적으로 생성되었습니다.",
                                              "status": 201,
                                              "data": {
                                                "id": 1,
                                                "title": "Spring Boot 시작하기",
                                                "content": "# Spring Boot 가이드\\n\\nSpring Boot는...\\n\\n## 설치 방법\\n...",
                                                "viewCount": 0,
                                                "authorId": 1,
                                                "authorName": "개발자",
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T10:30:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PostMapping
    public ApiResponse<MarkdownDetailResponse> createMarkdown(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody MarkdownCreateRequest request
    ) {
        log.info("마크다운 생성 API 호출 - 제목: {}", request.getTitle());

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        MarkdownDetailResponse response = markdownService.createMarkdown(request, user.getId(), user.getNickname());

        return ApiResponse.success("마크다운이 성공적으로 생성되었습니다.", response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "마크다운 개별 조회",
            description = "특정 ID의 마크다운 문서를 상세 조회합니다. (조회수 증가)\n\n" +
                    "경로 매개변수:\n" +
                    "- id: 조회할 마크다운의 ID (필수)"
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
                                              "message": "마크다운을 성공적으로 조회했습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "Spring Boot 시작하기",
                                                "content": "# Spring Boot 가이드\\n\\nSpring Boot는...\\n\\n## 설치 방법\\n...",
                                                "viewCount": 157,
                                                "authorId": 1,
                                                "authorName": "개발자",
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T14:20:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<MarkdownDetailResponse> getMarkdown(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "마크다운 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("마크다운 개별 조회 API 호출 - ID: {}", id);

        MarkdownDetailResponse response = markdownService.getMarkdown(id);

        return ApiResponse.success("마크다운을 성공적으로 조회했습니다.", response);
    }

    @Operation(
            summary = "마크다운 수정",
            description = "특정 ID의 마크다운 문서를 수정합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 마크다운만 수정 가능\n" +
                    "- ADMIN: 모든 마크다운 수정 가능\n\n" +
                    "경로 매개변수:\n" +
                    "- id: 수정할 마크다운의 ID (필수)\n\n" +
                    "요청 본문 구조:\n" +
                    "- title: 수정할 제목 (선택)\n" +
                    "- content: 수정할 내용 (선택)"
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
                                              "message": "마크다운이 성공적으로 수정되었습니다.",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "title": "Spring Boot 완전 가이드 (수정됨)",
                                                "content": "# Spring Boot 완전 가이드\\n\\n이 문서는 Spring Boot의 모든 것을 다룹니다...\\n\\n## 목차\\n1. 소개\\n2. 설치\\n3. 기본 설정\\n4. 고급 활용법",
                                                "viewCount": 156,
                                                "authorId": 1,
                                                "authorName": "개발자",
                                                "createdAt": "2025-08-26T10:30:00",
                                                "updatedAt": "2025-08-26T15:45:00"
                                              }
                                            }"""
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ApiResponse<MarkdownDetailResponse> updateMarkdown(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "마크다운 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody MarkdownUpdateRequest request
    ) {
        log.info("마크다운 수정 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        MarkdownDetailResponse response = markdownService.updateMarkdown(id, request, user.getId(), user.getRole());

        return ApiResponse.success("마크다운이 성공적으로 수정되었습니다.", response);
    }

    @Operation(
            summary = "마크다운 삭제",
            description = "특정 ID의 마크다운 문서를 삭제합니다.\n\n" +
                    "권한:\n" +
                    "- 작성자: 본인이 작성한 마크다운만 삭제 가능\n" +
                    "- ADMIN: 모든 마크다운 삭제 가능\n\n" +
                    "경로 매개변수:\n" +
                    "- id: 삭제할 마크다운의 ID (필수)"
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
                                              "message": "마크다운이 성공적으로 삭제되었습니다.",
                                              "status": 200
                                            }"""
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMarkdown(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(
                    name = "id",
                    description = "마크다운 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        log.info("마크다운 삭제 API 호출 - ID: {}", id);

        // JWT에서 사용자 정보 추출
        User user = authUtils.getUserFromAuthHeader(authorization);

        markdownService.deleteMarkdown(id, user.getId(), user.getRole());

        return ApiResponse.success("마크다운이 성공적으로 삭제되었습니다.");
    }

    @Operation(
            summary = "마크다운 제목 검색",
            description = "제목으로 마크다운 문서를 검색합니다.\n\n" +
                    "쿼리 매개변수:\n" +
                    "- title: 검색할 제목 키워드 (필수, 부분 일치 검색)\n\n" +
                    "사용 예시:\n" +
                    "- GET /api/admin/markdowns/search?title=Spring - 'Spring'이 포함된 제목 검색\n" +
                    "- GET /api/admin/markdowns/search?title=가이드 - '가이드'가 포함된 제목 검색"
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
                                              "message": "마크다운 검색을 성공적으로 완료했습니다.",
                                              "status": 200,
                                              "data": {
                                                "markdowns": [
                                                  {
                                                    "id": 1,
                                                    "title": "Spring Boot 완전 가이드",
                                                    "viewCount": 156,
                                                    "authorId": 1,
                                                    "authorName": "개발자",
                                                    "createdAt": "2025-08-26T10:30:00",
                                                    "updatedAt": "2025-08-26T15:45:00"
                                                  },
                                                  {
                                                    "id": 4,
                                                    "title": "Spring Security 설정법",
                                                    "viewCount": 78,
                                                    "authorId": 2,
                                                    "authorName": "관리자",
                                                    "createdAt": "2025-08-23T16:00:00",
                                                    "updatedAt": "2025-08-23T16:00:00"
                                                  }
                                                ]
                                              }
                                            }"""
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ApiResponse<MarkdownListResponseWrapper> searchMarkdowns(
            @Parameter(
                    name = "Authorization",
                    description = "Authorization 헤더 (Bearer token)",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "검색할 제목 키워드")
            @RequestParam String title
    ) {
        log.info("마크다운 제목 검색 API 호출 - 키워드: {}", title);

        List<MarkdownListResponse> markdowns = markdownService.searchMarkdownsByTitle(title);
        MarkdownListResponseWrapper responseData = MarkdownListResponseWrapper.of(markdowns);

        return ApiResponse.success("마크다운 검색을 성공적으로 완료했습니다.", responseData);
    }
}
