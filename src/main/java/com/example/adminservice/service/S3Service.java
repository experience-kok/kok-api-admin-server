package com.example.adminservice.service;

import com.example.adminservice.util.ImageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * AWS S3 관련 서비스
 * Presigned URL 생성 및 파일 업로드 관리
 * EC2 IAM Role을 통한 인증 사용
 */

@Slf4j
@Service
public class S3Service {

    @Autowired
    private ImageProcessor imageProcessor;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.bucket.prefix:}")
    private String bucketPrefix;

    @Value("${aws.s3.presigned-url.expiration}")
    private int presignedUrlExpirationSeconds;

    @Value("${aws.s3.banner.prefix}")
    private String bannerPrefix;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void initializeS3Client() {
        try {
            Region awsRegion = Region.of(region);

            // EC2 IAM Role을 통한 자동 인증
            this.s3Client = S3Client.builder()
                    .region(awsRegion)
                    .credentialsProvider(InstanceProfileCredentialsProvider.create())
                    .build();

            this.s3Presigner = S3Presigner.builder()
                    .region(awsRegion)
                    .credentialsProvider(InstanceProfileCredentialsProvider.create())
                    .build();

            log.info("S3 클라이언트 초기화 완료 (IAM Role 사용) - region: {}, bucket: {}, prefix: {}", 
                region, bucketName, bucketPrefix);
        } catch (Exception e) {
            log.error("S3 클라이언트 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 클라이언트 초기화에 실패했습니다.", e);
        }
    }

    /**
     * 버킷 prefix를 포함한 전체 키 생성
     */
    private String getFullKey(String key) {
        if (bucketPrefix == null || bucketPrefix.isEmpty()) {
            return key;
        }
        // prefix가 /로 끝나지 않으면 추가
        String prefix = bucketPrefix.endsWith("/") ? bucketPrefix : bucketPrefix + "/";
        return prefix + key;
    }


/**
     * S3Client Bean 등록 - S3CleanupService 등에서 사용
     */

    @Bean
    public S3Client s3Client() {
        if (this.s3Client == null) {
            Region awsRegion = Region.of(region);

            // EC2 IAM Role을 통한 자동 인증
            this.s3Client = S3Client.builder()
                    .region(awsRegion)
                    .credentialsProvider(InstanceProfileCredentialsProvider.create())
                    .build();
        }
        return this.s3Client;
    }

/**
     * 배너 이미지 업로드 후 16:9 비율로 크롭 처리
     */

    public String processBannerImageTo16x9(String originalS3Url) {
        try {
            log.info("배너 이미지 16:9 비율 처리 시작: {}", originalS3Url);

            // S3 URL에서 객체 키 추출
            String objectKey = extractObjectKeyFromUrl(originalS3Url);
            log.info("추출된 객체 키: {}", objectKey);

            // AWS SDK를 사용하여 직접 다운로드
            var getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            InputStream inputStream = s3Client.getObject(getObjectRequest);

            // 파일 확장자 추출
            String extension = imageProcessor.getFileExtension(originalS3Url);

            // 16:9 비율로 크롭
            ByteArrayOutputStream croppedImage = imageProcessor.cropTo16x9(inputStream, extension);

            // 새 파일명 생성
            String fileName = generateBannerFileName(extension);
            String s3Key = bannerPrefix + fileName;

            // S3에 크롭된 이미지 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(determineContentType(extension))
                    .build();

            s3Client.putObject(putRequest,
                RequestBody.fromBytes(croppedImage.toByteArray()));

            // 원본 이미지 삭제 (선택사항)
            deleteImageFromUrl(originalS3Url);

            String processedS3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, s3Key);

            log.info("16:9 비율 배너 이미지 처리 완료: {} -> {}", originalS3Url, processedS3Url);

            return processedS3Url;

        } catch (Exception e) {
            log.error("배너 이미지 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }


/**
     * S3 URL에서 객체 키 추출
     */
    private String extractObjectKeyFromUrl(String s3Url) {
        try {
            // presigned URL에서 쿼리 파라미터 제거
            String cleanUrl = s3Url.split("\\?")[0];

            // URL에서 객체 키 추출
            String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            if (cleanUrl.startsWith(prefix)) {
                return cleanUrl.substring(prefix.length());
            }

            // 대체 형식 처리 (s3://bucket/key 또는 다른 형식)
            if (cleanUrl.contains(".amazonaws.com/")) {
                return cleanUrl.substring(cleanUrl.lastIndexOf(".amazonaws.com/") + 15);
            }

            throw new IllegalArgumentException("유효하지 않은 S3 URL 형식: " + s3Url);
        } catch (Exception e) {
            log.error("S3 URL에서 객체 키 추출 실패: {}", e.getMessage());
            throw new RuntimeException("S3 URL 파싱 오류: " + e.getMessage(), e);
        }
    }


/**
     * 배너 파일명 생성
     */

    private String generateBannerFileName(String extension) {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("banner_%s_%s.%s", timestamp, uuid.substring(0, 8), extension);
    }


/**
     * S3 URL에서 이미지 삭제
     */

    private void deleteImageFromUrl(String s3Url) {
        try {
            String key = extractObjectKeyFromUrl(s3Url);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("원본 이미지 삭제 완료: {}", key);
        } catch (Exception e) {
            log.warn("원본 이미지 삭제 실패: {}", e.getMessage());
        }
    }


/**
     * 배너 이미지 업로드용 presigned URL 생성
     *
     * @param fileExtension 파일 확장자 (jpg, png 등)
     * @return presigned URL과 파일 키가 포함된 정보
     */

    public PresignedUrlResponse generateBannerPresignedUrl(String fileExtension) {
        try {
            // 고유한 파일명 생성
            String uuid = UUID.randomUUID().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = String.format("%s-%s.%s", timestamp, uuid, fileExtension);
            String objectKey = getFullKey(bannerPrefix + fileName);

            log.info("배너 이미지 presigned URL 생성 시작 - objectKey: {}", objectKey);

            // PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(determineContentType(fileExtension))
                    .build();

            // Presigned URL 요청 생성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Presigned URL 생성
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("배너 이미지 presigned URL 생성 완료 - objectKey: {}", objectKey);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .objectKey(objectKey)
                    .fileName(fileName)
                    .build();

        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Presigned URL 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }


/**
     * Kokpost 이미지 업로드용 presigned URL 생성
     *
     * @param fileExtension 파일 확장자 (jpg, png 등)
     * @return presigned URL과 파일 키가 포함된 정보
     */
    public PresignedUrlResponse generateKokpostPresignedUrl(String fileExtension) {
        try {
            // 고유한 파일명 생성 (타임스탬프 + UUID)
            String uuid = UUID.randomUUID().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = String.format("%s-%s.%s", timestamp, uuid, fileExtension);
            String objectKey = getFullKey("kokpost/" + fileName);

            log.info("Kokpost 이미지 presigned URL 생성 시작 - objectKey: {}", objectKey);

            // PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(determineContentType(fileExtension))
                    .build();

            // Presigned URL 요청 생성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(300)) // 5분
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Presigned URL 생성
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("Kokpost 이미지 presigned URL 생성 완료 - objectKey: {}", objectKey);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .objectKey(objectKey)
                    .fileName(fileName)
                    .build();

        } catch (Exception e) {
            log.error("Kokpost Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Presigned URL 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }


/**
     * 일반 이미지 업로드용 presigned URL 생성
     *
     * @param fileExtension 파일 확장자
     * @param folder 폴더 경로 (optional)
     * @return presigned URL 정보
     */

    public PresignedUrlResponse generatePresignedUrl(String fileExtension, String folder) {
        try {
            String uuid = UUID.randomUUID().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = String.format("%s-%s.%s", timestamp, uuid, fileExtension);

            String relativePath;
            if (folder != null && !folder.isEmpty()) {
                relativePath = folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;
            } else {
                relativePath = "uploads/" + fileName;
            }
            
            String objectKey = getFullKey(relativePath);

            log.info("일반 이미지 presigned URL 생성 시작 - objectKey: {}", objectKey);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(determineContentType(fileExtension))
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("일반 이미지 presigned URL 생성 완료 - objectKey: {}", objectKey);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .objectKey(objectKey)
                    .fileName(fileName)
                    .build();

        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Presigned URL 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }


/**
     * GET 작업용 presigned URL 생성
     *
     * @param objectKey S3 객체 키
     * @return GET용 presigned URL
     */

    public String generateGetPresignedUrl(String objectKey) {
        try {
            var getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            var getObjectPresignRequest = software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            var presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            log.error("GET용 Presigned URL 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("GET용 Presigned URL 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }


/**
     * S3 객체 URL 생성
     *
     * @param objectKey S3 객체 키
     * @return 공개 접근 가능한 URL
     */

    public String getObjectUrl(String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
    }


/**
     * 파일 확장자에 따른 Content-Type 결정
     *
     * @param fileExtension 파일 확장자
     * @return Content-Type
     */

    private String determineContentType(String fileExtension) {
        if (fileExtension == null) {
            return "application/octet-stream";
        }

        return switch (fileExtension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }


/**
     * S3 객체 존재 여부 확인
     *
     * @param objectKey 확인할 객체 키
     * @return 존재 여부
     */

    public boolean doesObjectExist(String objectKey) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(objectKey));
            return true;
        } catch (Exception e) {
            log.warn("S3 객체 존재 확인 실패: {} - {}", objectKey, e.getMessage());
            return false;
        }
    }


/**
     * S3 객체 정보 조회
     *
     * @param objectKey 조회할 객체 키
     * @return 객체 정보
     */

    public String getObjectInfo(String objectKey) {
        try {
            var response = s3Client.headObject(builder -> builder.bucket(bucketName).key(objectKey));
            return String.format("Object: %s, Size: %d bytes, LastModified: %s",
                objectKey, response.contentLength(), response.lastModified());
        } catch (Exception e) {
            log.error("S3 객체 정보 조회 실패: {} - {}", objectKey, e.getMessage());
            return "Object not found: " + objectKey;
        }
    }


/**
     * S3 객체 삭제
     *
     * @param objectKey 삭제할 객체 키
     */

    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(objectKey));
            log.info("S3 객체 삭제 완료: {}", objectKey);
        } catch (Exception e) {
            log.error("S3 객체 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 객체 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }


/**
     * Presigned URL 응답 DTO
     */

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PresignedUrlResponse {
        private String presignedUrl;
        private String objectKey;
        private String fileName;
        private String publicUrl;

        public String getPublicUrl() {
            if (this.publicUrl == null && this.objectKey != null) {
                return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s",
                    "kok-main-service-bucket", this.objectKey);
            }
            return this.publicUrl;
        }
    }
}
