package com.example.adminservice.controller;

import com.example.adminservice.common.BaseResponse;
import com.example.adminservice.dto.BannerPresignedUrlRequest;
import com.example.adminservice.dto.KokpostPresignedUrlRequest;
import com.example.adminservice.dto.PresignedUrlRequest;
import com.example.adminservice.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 API", description = "AWS S3를 통한 이미지 업로드 관련 API")
public class ImageUploadController {

    private final S3Service s3Service;

    @Operation(
            summary = "Kokpost 이미지 업로드용 Presigned URL 생성",
            description = """
            S3에 kokpost 폴더에 이미지를 업로드하기 위한 presigned URL을 생성합니다.
            
            
            ### 지원 파일 형식
            - jpg, jpeg, png
            
            ### 제한사항
            - URL 유효 시간: 5분
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "Kokpost 이미지 업로드용 URL 생성 성공",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "배너 이미지 업로드용 URL이 성공적으로 생성되었습니다.",
                                      "status": 200,
                                      "data": {
                                        "presignedUrl": "https://ckokservice.s3.ap-northeast-2.amazonaws.com/kokpost/1756804652981-e3601f15-d8e3-4360-aa41-e8229402d4e5.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
                                      }
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
                            examples = {
                                    @ExampleObject(
                                            name = "지원하지 않는 파일 형식",
                                            summary = "허용되지 않은 파일 확장자인 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "지원하지 않는 파일 형식입니다. 허용 형식: jpg, jpeg, png, gif, webp",
                                              "errorCode": "UNSUPPORTED_FILE_TYPE",
                                              "status": 400
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "URL 생성 실패",
                                    summary = "presigned URL 생성 중 오류가 발생한 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "PreSigned URL 생성 중 오류가 발생했습니다.",
                                      "errorCode": "URL_GENERATION_FAILED",
                                      "status": 500
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/kokpost/presigned-url")
    public ResponseEntity<?> generateKokpostPresignedUrl(
            @Parameter(description = "파일 정보", required = true)
            @Valid @RequestBody KokpostPresignedUrlRequest request
    ) {
        try {
            log.info("Kokpost 이미지 Presigned URL 요청: fileExtension={}", request.getFileExtension());

            // S3 Presigned URL 생성
            S3Service.PresignedUrlResponse response = s3Service.generateKokpostPresignedUrl(request.getFileExtension());

            log.info("Kokpost 이미지 Presigned URL 생성 완료");

            // 응답 데이터 구성
            Map<String, Object> responseData = Map.of(
                    "presignedUrl", response.getPresignedUrl()
            );

            return ResponseEntity.ok(
                    BaseResponse.success(
                            responseData,
                            "배너 이미지 업로드용 URL이 성공적으로 생성되었습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail(e.getMessage(), "VALIDATION_ERROR", 400));
        } catch (Exception e) {
            log.error("Kokpost Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("PreSigned URL 생성 중 오류가 발생했습니다.", "URL_GENERATION_FAILED", 500));
        }
    }

    @Operation(
            summary = "배너 이미지 업로드용 Presigned URL 생성",
            description = """
            S3에 배너 이미지를 업로드하기 위한 presigned URL을 생성합니다.
            
            ### 사용 방법
            1. 이 API로 presigned URL을 받습니다
            2. 받은 URL로 직접 이미지를 PUT 요청으로 업로드합니다
            3. 업로드 완료 후 S3 URL을 배너 생성 API에 사용합니다
            
            ### 지원 파일 형식
            - jpg, jpeg, png
            
            ### 제한사항
            - 최대 파일 크기: 10MB
            - URL 유효 시간: 15분
            
            ### 권한
            - ADMIN 권한 필요
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    summary = "배너 이미지 업로드용 URL 생성 성공",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "배너 이미지 업로드용 URL이 성공적으로 생성되었습니다",
                                      "status": 200,
                                      "data": {
                                        "presignedUrl": "https://ckokservice.s3.ap-northeast-2.amazonaws.com/banners/banner_20250714_164523_abc123.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
                                      }
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
                            examples = {
                                    @ExampleObject(
                                            name = "지원하지 않는 파일 형식",
                                            summary = "허용되지 않은 파일 확장자인 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "지원하지 않는 파일 형식입니다. 허용 형식: jpg, jpeg, png, gif, webp",
                                              "errorCode": "UNSUPPORTED_FILE_TYPE",
                                              "status": 400
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "필수 필드 누락",
                                            summary = "fileExtension 필드가 없는 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "파일 확장자는 필수입니다",
                                              "errorCode": "VALIDATION_ERROR",
                                              "status": 400
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    summary = "JWT 토큰이 만료된 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "토큰이 만료되었습니다. 다시 로그인 해주세요",
                                      "errorCode": "TOKEN_EXPIRED",
                                      "status": 401
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "관리자 권한 없음",
                                    summary = "ADMIN 권한이 없는 사용자가 접근한 경우",
                                    value = """
                                    {
                                      "success": false,
                                      "message": "배너 이미지 업로드는 관리자만 가능합니다. 현재 권한: USER",
                                      "errorCode": "FORBIDDEN",
                                      "status": 403
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "AWS S3 연결 오류",
                                            summary = "AWS S3 서비스 연결 문제가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "Presigned URL 생성에 실패했습니다: AWS S3 서비스 연결 오류",
                                              "errorCode": "AWS_S3_ERROR",
                                              "status": 500
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "URL 생성 실패",
                                            summary = "presigned URL 생성 중 오류가 발생한 경우",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "Presigned URL 생성에 실패했습니다",
                                              "errorCode": "URL_GENERATION_FAILED",
                                              "status": 500
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/banners/presigned-url")
    public ResponseEntity<?> generateBannerPresignedUrl(
            @Parameter(description = "파일 정보", required = true)
            @Valid @RequestBody BannerPresignedUrlRequest request
    ) {
        try {
            log.info("배너 이미지 Presigned URL 요청: fileExtension={}", request.getFileExtension());

            // S3 Presigned URL 생성
            S3Service.PresignedUrlResponse response = s3Service.generateBannerPresignedUrl(request.getFileExtension());

            log.info("배너 이미지 Presigned URL 생성 완료");

            // 응답 데이터 구성
            Map<String, Object> responseData = Map.of(
                    "presignedUrl", response.getPresignedUrl()
            );

            return ResponseEntity.ok(
                    BaseResponse.success(
                            responseData,
                            "배너 이미지 업로드용 URL이 성공적으로 생성되었습니다."
                    )
            );
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.fail(e.getMessage(), "VALIDATION_ERROR", 400));
        } catch (Exception e) {
            log.error("배너 Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("Presigned URL 생성에 실패했습니다.", "URL_GENERATION_FAILED", 500));
        }
    }



}
