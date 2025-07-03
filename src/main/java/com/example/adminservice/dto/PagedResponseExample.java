package com.example.adminservice.dto;

import com.example.adminservice.common.BaseResponse;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * 새로운 페이지네이션 응답 구조 예시
 */
@Data
public class PagedResponseExample {
    
    /**
     * 예시 응답 JSON 구조:
     * {
     *   "success": true,
     *   "message": "사용자 목록 조회 성공",
     *   "status": 200,
     *   "data": {
     *     "content": [
     *       {
     *         "id": 1,
     *         "email": "user1@example.com",
     *         "nickname": "사용자1",
     *         "role": "USER",
     *         "active": true
     *       }
     *     ],
     *     "pagination": {
     *       "pageNumber": 1,
     *       "pageSize": 10,
     *       "totalPages": 5,
     *       "totalElements": 45,
     *       "first": true,
     *       "last": false
     *     }
     *   }
     * }
     */
    
    public static BaseResponse.Success<BaseResponse.PagedData<List<UserListResponseDTO>>> createExampleResponse() {
        // 예시 사용자 데이터
        List<UserListResponseDTO> users = Arrays.asList(
            UserListResponseDTO.builder()
                .id(1L)
                .email("user1@example.com")
                .nickname("사용자1")
                .role("USER")
                .active(true)
                .build(),
            UserListResponseDTO.builder()
                .id(2L)
                .email("user2@example.com")
                .nickname("사용자2")
                .role("CLIENT")
                .active(true)
                .build()
        );
        
        // 페이지네이션 정보
        BaseResponse.Pagination pagination = new BaseResponse.Pagination(
            1,      // pageNumber (1-based)
            10,     // pageSize
            5,      // totalPages
            45L,    // totalElements
            true,   // first
            false   // last
        );
        
        // PagedData 생성
        BaseResponse.PagedData<List<UserListResponseDTO>> pagedData = 
            new BaseResponse.PagedData<>(users, pagination);
        
        // 최종 응답
        return BaseResponse.success(pagedData, "사용자 목록 조회 성공");
    }
}
