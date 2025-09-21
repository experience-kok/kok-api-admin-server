package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.domain.Company;
import com.example.adminservice.domain.User;
import com.example.adminservice.dto.CompanyDetailResponse;
import com.example.adminservice.repository.CompanyRepository;
import com.example.adminservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 업체 관리 컨트롤러
 * USER 롤을 가진 사용자들이 등록한 회사 정보를 관리합니다.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "클라이언트 심사 요청 인원 API", description = "클라이언트 심사 요청 인원 목록 API.")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Operation(
            summary = "업체 목록 조회 (페이지네이션)",
            description = "클라이언트로 권한 승급을 요청한 인원 목록 조회.\n\n" +
                    "### 주요 기능\n" +
                    "- 업체명, 사업자등록번호, 담당자 정보 조회\n" +
                    "- 페이지네이션 지원 (기본 20개씩)\n" +
                    "- 정렬 옵션 지원 (ID, 업체명, 등록일 등)\n\n" +
                    "### 정렬 옵션\n" +
                    "- id: 업체 ID 순 (기본값)\n" +
                    "- companyName: 업체명 순\n" +
                    "- createdAt: 등록일 순\n" +
                    "- businessRegistrationNumber: 사업자등록번호 순",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업체 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "업체 목록 조회 성공",
                                    summary = "페이지네이션된 업체 목록",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "업체 목록 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "companies": [
                                                  {
                                                    "id": 1,
                                                    "companyName": "㈜체험콕테스트",
                                                    "businessRegistrationNumber": "123-45-67890",
                                                    "contactPerson": "김담당자",
                                                    "phoneNumber": "02-1234-5678",
                                                    "userId": 10,
                                                    "createdAt": "2025-08-15T10:30:00",
                                                    "updatedAt": "2025-08-20T14:20:00"
                                                  },
                                                  {
                                                    "id": 2,
                                                    "companyName": "맛집탐방 주식회사",
                                                    "businessRegistrationNumber": "987-65-43210",
                                                    "contactPerson": "이사장님",
                                                    "phoneNumber": "010-9876-5432",
                                                    "userId": 25,
                                                    "createdAt": "2025-08-10T09:15:00",
                                                    "updatedAt": "2025-08-18T16:45:00"
                                                  }
                                                ],
                                                "pagination": {
                                                  "pageNumber": 1,
                                                  "pageSize": 20,
                                                  "totalPages": 3,
                                                  "totalElements": 58,
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
    @GetMapping("/examine")
    public ResponseEntity<?> getUserCompanies(
            @Parameter(
                    name = "Authorization",
                    description = "JWT 인증 토큰 (Bearer {token})",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String authorization,

            @Parameter(
                    description = "페이지 번호 (0부터 시작)",
                    example = "0",
                    schema = @Schema(minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "페이지당 항목 수 (최대 100개)",
                    example = "20",
                    schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20")
            )
            @RequestParam(defaultValue = "20") int size,

            @Parameter(
                    description = "정렬 기준 필드",
                    example = "id",
                    schema = @Schema(allowableValues = {"id", "companyName", "createdAt", "businessRegistrationNumber"}, defaultValue = "id")
            )
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(
                    description = "정렬 방향",
                    example = "desc",
                    schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "desc")
            )
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            log.info("업체 목록 조회 요청 - page: {}, size: {}, sortBy: {}, sortDir: {}",
                    page, size, sortBy, sortDir);

            // 입력 검증
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.fail("페이지 번호는 0 이상이어야 합니다.", "INVALID_PARAMETER", 400));
            }

            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.fail("페이지 크기는 1~100 사이여야 합니다.", "INVALID_PARAMETER", 400));
            }

            // 정렬 방향 설정
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;

            // 정렬 필드 검증
            if (!isValidSortField(sortBy)) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.fail("유효하지 않은 정렬 필드입니다. 허용된 값: id, companyName, createdAt, businessRegistrationNumber", "INVALID_SORT_FIELD", 400));
            }

            // 페이징 설정
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // USER 롤을 가진 사용자들의 회사 목록 조회
            Page<Company> companiesPage = companyRepository.findCompaniesByUserRole(pageable);

            log.info("업체 목록 조회 성공 - 총 {}개 업체, 현재 페이지: {}/{}",
                    companiesPage.getTotalElements(),
                    page + 1,
                    companiesPage.getTotalPages());

            return ResponseEntity.ok(BaseResponse.success(createPagedCompaniesResponse(companiesPage), "업체 목록 조회 성공"));

        } catch (Exception e) {
            log.error("업체 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.fail("업체 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "업체 상세 정보 조회",
            description = "특정 업체의 상세 정보를 조회합니다.\n\n" +
                    "### 주요 기능\n" +
                    "- 업체 ID로 상세 정보 조회\n" +
                    "- 업체 기본 정보 및 등록자 정보 포함\n" +
                    "- 존재하지 않는 업체 ID에 대한 에러 처리\n\n" +
                    "### 응답 정보\n" +
                    "- 업체 기본 정보 (이름, 사업자등록번호, 담당자 등)\n" +
                    "- 등록자 정보 (사용자 ID, 등록일 등)\n" +
                    "- 최종 수정일 정보",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업체 상세 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "업체 상세 정보 조회 성공",
                                    summary = "업체 상세 정보",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "업체 상세 정보 조회 성공",
                                              "status": 200,
                                              "data": {
                                                "id": 1,
                                                "companyName": "㈜체험콕테스트",
                                                "businessRegistrationNumber": "123-45-67890",
                                                "contactPerson": "김담당자",
                                                "phoneNumber": "02-1234-5678",
                                                "userId": 10,
                                                "createdAt": "2025-08-15T10:30:00",
                                                "updatedAt": "2025-08-20T14:20:00",
                                                "userInfo": {
                                                  "id": 10,
                                                  "email": "company@example.com",
                                                  "nickname": "김회사대표",
                                                  "role": "USER",
                                                  "active": true,
                                                  "phone": "010-1234-5678",
                                                  "gender": "male",
                                                  "age": 35,
                                                  "provider": "kakao",
                                                  "emailVerified": true,
                                                  "profileImg": "https://example.com/profile/company-user.jpg",
                                                  "createdAt": "2025-08-10T09:00:00",
                                                  "updatedAt": "2025-08-15T14:20:00"
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "업체를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "업체 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 ID의 업체를 찾을 수 없습니다.",
                                              "status": 404,
                                              "errorCode": "COMPANY_NOT_FOUND"
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
                                    name = "잘못된 업체 ID",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "올바른 업체 ID를 입력해주세요.",
                                              "status": 400,
                                              "errorCode": "INVALID_COMPANY_ID"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{companyId}")
    public ResponseEntity<?> getCompanyDetail(
            @Parameter(
                    name = "Authorization",
                    description = "JWT 인증 토큰 (Bearer {token})",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String authorization,

            @Parameter(
                    description = "조회할 업체의 ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long companyId) {

        try {
            log.info("업체 상세 정보 조회 요청 - companyId: {}", companyId);

            // 입력 검증
            if (companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.fail("올바른 업체 ID를 입력해주세요.", "INVALID_COMPANY_ID", 400));
            }

            // 업체 정보 조회
            Company company = companyRepository.findById(companyId)
                    .orElse(null);

            if (company == null) {
                log.warn("업체를 찾을 수 없음 - companyId: {}", companyId);
                return ResponseEntity.status(404)
                        .body(BaseResponse.fail("해당 ID의 업체를 찾을 수 없습니다.", "COMPANY_NOT_FOUND", 404));
            }

            // 등록자 정보 조회
            User user = userRepository.findById(company.getUserId()).orElse(null);

            // DTO 변환
            CompanyDetailResponse response = convertToDetailResponse(company, user);

            log.info("업체 상세 정보 조회 성공 - companyId: {}, companyName: {}",
                    companyId, company.getCompanyName());

            return ResponseEntity.ok(BaseResponse.success(response, "업체 상세 정보 조회 성공"));

        } catch (Exception e) {
            log.error("업체 상세 정보 조회 중 오류 발생 - companyId: {}", companyId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.fail("업체 상세 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * Company 엔티티를 CompanyDetailResponse DTO로 변환
     */
    private CompanyDetailResponse convertToDetailResponse(Company company, User user) {
        CompanyDetailResponse response = new CompanyDetailResponse();
        response.setId(company.getId());
        response.setCompanyName(company.getCompanyName());
        response.setBusinessRegistrationNumber(company.getBusinessRegistrationNumber());
        response.setContactPerson(company.getContactPerson());
        response.setPhoneNumber(company.getPhoneNumber());
        response.setUserId(company.getUserId());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());

        // 사용자 정보 포함
        if (user != null) {
            CompanyDetailResponse.UserSummaryDTO userInfo = new CompanyDetailResponse.UserSummaryDTO();
            userInfo.setId(user.getId());
            userInfo.setEmail(user.getEmail());
            userInfo.setNickname(user.getNickname());
            userInfo.setRole(user.getRole().toString());
            userInfo.setActive(user.getActive());
            userInfo.setPhone(user.getPhone());
            userInfo.setGender(user.getGender());
            userInfo.setAge(user.getAge());
            userInfo.setProvider(user.getProvider());
            userInfo.setEmailVerified(user.getEmailVerified());
            userInfo.setProfileImg(user.getProfileImg());  // 프로필 이미지 추가
            userInfo.setCreatedAt(user.getCreatedAt());
            userInfo.setUpdatedAt(user.getUpdatedAt());

            response.setUserInfo(userInfo);
        }

        return response;
    }

    /**
     * 페이지된 업체 목록 응답 생성 (companies 필드명 사용)
     */
    private Object createPagedCompaniesResponse(Page<Company> companiesPage) {
        return new Object() {
            public final List<Company> companies = companiesPage.getContent();
            public final Object pagination = new Object() {
                public final int pageNumber = companiesPage.getNumber() + 1;  // 1-based
                public final int pageSize = companiesPage.getSize();
                public final int totalPages = companiesPage.getTotalPages();
                public final long totalElements = companiesPage.getTotalElements();
                public final boolean first = companiesPage.isFirst();
                public final boolean last = companiesPage.isLast();
            };
        };
    }

    /**
     * 정렬 필드 유효성 검증
     */
    private boolean isValidSortField(String sortBy) {
        return sortBy != null &&
                (sortBy.equals("id") ||
                        sortBy.equals("companyName") ||
                        sortBy.equals("createdAt") ||
                        sortBy.equals("businessRegistrationNumber"));
    }
}
