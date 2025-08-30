package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@Schema(description = "사용자 메모 업데이트 요청")
public class UserMemoUpdateRequest {
    
    @Schema(description = "메모 내용", example = "VIP 고객, 특별 관리 필요")
    @Size(max = 1000, message = "메모는 1000자 이하로 입력해주세요")
    private String memo;
}
