package com.example.adminservice.controller;

import com.example.adminservice.domain.Company;
import com.example.adminservice.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 업체 관리 컨트롤러
 * 
 * 응답 예시:
 * {
 *   "success": true,
 *   "message": "USER 롤 사용자의 회사 목록 조회 성공",
 *   "status": 200,
 *   "data": {
 *     "companies": [
 *       {
 *         "id": 1,
 *         "companyName": "테스트 회사",
 *         "businessRegistrationNumber": "123-45-67890",
 *         "userId": 10,
 *         "createdAt": "2025-01-15T10:00:00",
 *         "updatedAt": "2025-01-15T10:00:00"
 *       }
 *     ],
 *     "currentPage": 0,
 *     "totalPages": 1,
 *     "totalElements": 1,
 *     "size": 20,
 *     "hasNext": false,
 *     "hasPrevious": false
 *   }
 * }
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "업체 관리 API", description = "업체 관리 API")
public class CompanyController {

    private final CompanyRepository companyRepository;

    /**
     * USER 롤을 가진 사용자들의 회사 목록 조회
     */
    @GetMapping("/user-companies")
    @Operation(summary = "USER 롤 사용자의 회사 목록 조회", 
               description = "USER 롤을 가진 사용자들이 등록한 회사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> getUserCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            log.info("USER 롤 사용자의 회사 목록 조회 요청 - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                     page, size, sortBy, sortDir);

            // 정렬 방향 설정
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            // 페이징 설정
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // USER 롤을 가진 사용자들의 회사 목록 조회
            Page<Company> companiesPage = companyRepository.findCompaniesByUserRole(pageable);
            
            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "USER 롤 사용자의 회사 목록 조회 성공");
            response.put("status", 200);
            
            Map<String, Object> data = new HashMap<>();
            data.put("companies", companiesPage.getContent());
            data.put("currentPage", companiesPage.getNumber());
            data.put("totalPages", companiesPage.getTotalPages());
            data.put("totalElements", companiesPage.getTotalElements());
            data.put("size", companiesPage.getSize());
            data.put("hasNext", companiesPage.hasNext());
            data.put("hasPrevious", companiesPage.hasPrevious());
            
            response.put("data", data);
            
            log.info("USER 롤 사용자의 회사 목록 조회 성공 - 총 {}개", companiesPage.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("USER 롤 사용자의 회사 목록 조회 중 오류 발생", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "회사 목록 조회 중 오류가 발생했습니다.");
            errorResponse.put("status", 500);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
