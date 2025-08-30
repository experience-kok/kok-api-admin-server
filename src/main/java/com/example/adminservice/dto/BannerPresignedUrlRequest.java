package com.example.adminservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 배너 이미지 Presigned URL 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배너 이미지 Presigned URL 생성 요청")
public class BannerPresignedUrlRequest {

    @Schema(description = "파일 확장자", example = "jpg", allowableValues = {"jpg", "jpeg", "png", "gif", "webp"})
    @NotBlank(message = "파일 확장자는 필수입니다")
    @Pattern(regexp = "^(jpg|jpeg|png|gif|webp|bmp|svg)$", 
             message = "지원하지 않는 파일 형식입니다. jpg, jpeg, png, gif, webp, bmp, svg만 허용됩니다.")
    private String fileExtension;
}
