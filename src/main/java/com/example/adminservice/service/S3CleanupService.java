package com.example.adminservice.service;

import com.example.adminservice.repository.BannerImageRepository;
import com.example.adminservice.repository.KokPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * S3 파일 자동 정리 서비스
 * DB에 등록되지 않은 S3 파일들을 자동으로 삭제합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3CleanupService {

    private final S3Client s3Client;
    private final BannerImageRepository bannerImageRepository;
    private final KokPostRepository kokPostRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.banner.prefix}")
    private String bannerPrefix;

    // kokpost 폴더 경로
    private static final String KOKPOST_PREFIX = "kokpost/";

    /**
     * 매일 새벽 3시에 DB에 없는 파일들을 자동 삭제
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void cleanupUnusedFiles() {
        try {
            log.info("🧹 S3 자동 정리 시작 - DB에 없는 파일들 삭제");

            int bannerDeletedCount = deleteFilesNotInDatabase();
            int kokpostDeletedCount = deleteUnusedKokpostImages();
            int totalDeleted = bannerDeletedCount + kokpostDeletedCount;

            if (totalDeleted > 0) {
                log.info("✅ S3 자동 정리 완료: 총 {}개 파일 삭제됨 (배너: {}개, kokpost: {}개)",
                        totalDeleted, bannerDeletedCount, kokpostDeletedCount);
            } else {
                log.info("📋 S3 자동 정리 완료: 삭제할 파일 없음");
            }

        } catch (Exception e) {
            log.error("❌ S3 자동 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * DB에 등록되지 않은 파일들을 삭제
     */
    private int deleteFilesNotInDatabase() {
        try {
            // 1. S3에서 모든 배너 파일 목록 가져오기
            List<S3Object> s3Objects = getAllBannerFiles();
            log.info("S3 배너 파일 총 {}개 발견", s3Objects.size());

            // 2. 데이터베이스에서 사용중인 파일 키들 가져오기
            Set<String> usedKeys = getUsedBannerKeys();
            log.info("데이터베이스에 등록된 배너 파일 {}개", usedKeys.size());

            // 3. DB에 없는 파일들 찾기
            List<S3Object> filesToDelete = s3Objects.stream()
                    .filter(obj -> !usedKeys.contains(obj.key()))
                    .collect(Collectors.toList());

            log.info("DB에 없는 파일 {}개 발견", filesToDelete.size());

            // 4. 파일들 삭제
            int deletedCount = 0;
            for (S3Object obj : filesToDelete) {
                try {
                    deleteS3Object(obj.key());
                    deletedCount++;
                    log.info("🗑️ 삭제됨: {}", obj.key());
                } catch (Exception e) {
                    log.warn("⚠️ 파일 삭제 실패: {} - {}", obj.key(), e.getMessage());
                }
            }

            return deletedCount;

        } catch (Exception e) {
            log.error("S3 파일 정리 중 오류: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * S3에서 모든 배너 파일 목록 가져오기
     */
    private List<S3Object> getAllBannerFiles() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(bannerPrefix)
                    .build();

            List<S3Object> allObjects = new ArrayList<>();
            ListObjectsV2Response response;

            do {
                response = s3Client.listObjectsV2(listRequest);
                allObjects.addAll(response.contents());

                listRequest = listRequest.toBuilder()
                        .continuationToken(response.nextContinuationToken())
                        .build();
            } while (response.isTruncated());

            return allObjects;

        } catch (Exception e) {
            log.error("S3 파일 목록 조회 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 데이터베이스에서 사용중인 배너 파일 키들 가져오기
     */
    private Set<String> getUsedBannerKeys() {
        try {
            return bannerImageRepository.findAll().stream()
                    .map(banner -> banner.getBannerUrl())
                    .filter(Objects::nonNull)
                    .map(this::extractObjectKeyFromUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("사용중인 배너 키 조회 실패: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * URL에서 S3 객체 키 추출
     */
    private String extractObjectKeyFromUrl(String url) {
        try {
            if (url == null) return null;

            // presigned URL에서 쿼리 파라미터 제거
            String cleanUrl = url.split("\\?")[0];

            // S3 URL에서 객체 키 추출
            String prefix = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/", bucketName);
            if (cleanUrl.startsWith(prefix)) {
                return cleanUrl.substring(prefix.length());
            }

            return null;
        } catch (Exception e) {
            log.warn("URL에서 객체 키 추출 실패: {} - {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * S3 객체 삭제
     */
    private void deleteS3Object(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.debug("S3 객체 삭제 완료: {}", objectKey);

        } catch (Exception e) {
            log.error("S3 객체 삭제 실패: {} - {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("S3 객체 삭제 실패: " + objectKey, e);
        }
    }

    /**
     * kokpost 폴더의 사용하지 않는 이미지 삭제
     */
    private int deleteUnusedKokpostImages() {
        try {
            // 1. S3에서 모든 kokpost 이미지 목록 가져오기
            List<S3Object> s3Objects = getAllKokpostFiles();
            log.info("S3 kokpost 이미지 총 {}개 발견", s3Objects.size());

            // 2. 데이터베이스에서 사용중인 이미지 URL들 가져오기
            Set<String> usedImageUrls = getUsedKokpostImageUrls();
            log.info("데이터베이스에서 사용중인 kokpost 이미지 {}개", usedImageUrls.size());

            // 3. DB에 없는 파일들 찾기
            List<S3Object> filesToDelete = s3Objects.stream()
                    .filter(obj -> !usedImageUrls.contains(getS3UrlFromKey(obj.key())))
                    .collect(Collectors.toList());

            log.info("DB에 없는 kokpost 이미지 {}개 발견", filesToDelete.size());

            // 4. 파일들 삭제
            int deletedCount = 0;
            for (S3Object obj : filesToDelete) {
                try {
                    deleteS3Object(obj.key());
                    deletedCount++;
                    log.info("🗑️ Kokpost 이미지 삭제됨: {}", obj.key());
                } catch (Exception e) {
                    log.warn("⚠️ Kokpost 이미지 삭제 실패: {} - {}", obj.key(), e.getMessage());
                }
            }

            return deletedCount;

        } catch (Exception e) {
            log.error("Kokpost 이미지 정리 중 오류: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * S3에서 모든 kokpost 이미지 목록 가져오기
     */
    private List<S3Object> getAllKokpostFiles() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(KOKPOST_PREFIX)
                    .build();

            List<S3Object> allObjects = new ArrayList<>();
            ListObjectsV2Response response;

            do {
                response = s3Client.listObjectsV2(listRequest);
                allObjects.addAll(response.contents());

                listRequest = listRequest.toBuilder()
                        .continuationToken(response.nextContinuationToken())
                        .build();
            } while (response.isTruncated());

            return allObjects;

        } catch (Exception e) {
            log.error("Kokpost S3 파일 목록 조회 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 데이터베이스에서 사용중인 kokpost 이미지 URL들 가져오기
     */
    private Set<String> getUsedKokpostImageUrls() {
        try {
            return kokPostRepository.findAll().stream()
                    .map(kokPost -> kokPost.getContent())
                    .filter(Objects::nonNull)
                    .flatMap(content -> extractImageUrlsFromContent(content).stream())
                    .filter(url -> url.contains("kokpost/"))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("사용중인 kokpost 이미지 URL 조회 실패: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 마크다운/HTML 컨텐츠에서 이미지 URL 추출
     */
    private Set<String> extractImageUrlsFromContent(String content) {
        Set<String> imageUrls = new HashSet<>();

        // 마크다운 이미지 패턴: ![alt](url)
        Pattern markdownPattern = Pattern.compile("!\\[.*?\\]\\((https?://[^)]+)\\)");
        Matcher markdownMatcher = markdownPattern.matcher(content);
        while (markdownMatcher.find()) {
            imageUrls.add(markdownMatcher.group(1));
        }

        // HTML img 태그 패턴: <img src="url">
        Pattern htmlPattern = Pattern.compile("<img[^>]*src=[\"'](https?://[^\"']+)[\"'][^>]*>");
        Matcher htmlMatcher = htmlPattern.matcher(content);
        while (htmlMatcher.find()) {
            imageUrls.add(htmlMatcher.group(1));
        }

        return imageUrls;
    }

    /**
     * S3 키에서 전체 URL 생성
     */
    private String getS3UrlFromKey(String key) {
        return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, key);
    }
}
