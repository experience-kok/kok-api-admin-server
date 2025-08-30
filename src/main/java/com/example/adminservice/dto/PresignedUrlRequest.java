package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Presigned URL 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Presigned URL 생성 요청")
public class PresignedUrlRequest {

    @Schema(description = "파일 확장자", example = "jpg", allowableValues = {"jpg", "jpeg", "png", "gif", "webp"})
    @NotBlank(message = "파일 확장자는 필수입니다")
    @Pattern(regexp = "^(jpg|jpeg|png|gif|webp|bmp|svg)$", 
             message = "지원하지 않는 파일 형식입니다. jpg, jpeg, png, gif, webp, bmp, svg만 허용됩니다.")
    private String fileExtension;

    @Schema(description = "업로드 폴더 경로 (선택사항)", example = "banners")
    private String folder;
}
