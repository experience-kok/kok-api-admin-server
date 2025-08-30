package com.example.adminservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * S3 URL 처리 유틸리티
 */
@Slf4j
@Component
public class S3UrlUtils {

    /**
     * Presigned URL에서 쿼리 파라미터를 제거하여 깨끗한 S3 URL을 반환합니다.
     * 
     * @param presignedUrl Presigned URL (쿼리 파라미터 포함)
     * @return 깨끗한 S3 URL (쿼리 파라미터 제거됨)
     */
    public static String cleanS3Url(String presignedUrl) {
        if (presignedUrl == null || presignedUrl.trim().isEmpty()) {
            return presignedUrl;
        }
        
        try {
            // '?' 문자를 기준으로 split하여 첫 번째 부분만 반환
            String cleanUrl = presignedUrl.split("\\?")[0];
            log.debug("URL 정리: {} -> {}", presignedUrl, cleanUrl);
            return cleanUrl;
        } catch (Exception e) {
            log.warn("URL 정리 중 오류 발생, 원본 반환: {}", e.getMessage());
            return presignedUrl;
        }
    }

    /**
     * S3 URL이 유효한지 검증합니다.
     * 
     * @param s3Url 검증할 S3 URL
     * @return 유효한 S3 URL인지 여부
     */
    public static boolean isValidS3Url(String s3Url) {
        if (s3Url == null || s3Url.trim().isEmpty()) {
            return false;
        }
        
        String cleanUrl = cleanS3Url(s3Url);
        
        // 기본적인 S3 URL 패턴 검증
        return cleanUrl.matches("https://[a-zA-Z0-9.-]+\\.s3\\.[a-zA-Z0-9-]+\\.amazonaws\\.com/.+");
    }

    /**
     * S3 URL에서 버킷명을 추출합니다.
     * 
     * @param s3Url S3 URL
     * @return 버킷명 (추출 실패시 null)
     */
    public static String extractBucketName(String s3Url) {
        try {
            String cleanUrl = cleanS3Url(s3Url);
            
            // https://bucket-name.s3.region.amazonaws.com/key 패턴에서 bucket-name 추출
            String[] parts = cleanUrl.replace("https://", "").split("\\.");
            if (parts.length >= 3 && parts[1].equals("s3")) {
                return parts[0];
            }
            
            return null;
        } catch (Exception e) {
            log.warn("버킷명 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * S3 URL에서 객체 키를 추출합니다.
     * 
     * @param s3Url S3 URL
     * @return 객체 키 (추출 실패시 null)
     */
    public static String extractObjectKey(String s3Url) {
        try {
            String cleanUrl = cleanS3Url(s3Url);
            
            // https://bucket.s3.region.amazonaws.com/object/key 에서 object/key 부분 추출
            int lastSlashIndex = cleanUrl.indexOf(".amazonaws.com/");
            if (lastSlashIndex != -1) {
                return cleanUrl.substring(lastSlashIndex + 15); // ".amazonaws.com/" 길이가 15
            }
            
            return null;
        } catch (Exception e) {
            log.warn("객체 키 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 파일 확장자를 추출합니다.
     * 
     * @param url 파일 URL
     * @return 파일 확장자 (소문자, 점 제외)
     */
    public static String extractFileExtension(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        try {
            String cleanUrl = cleanS3Url(url);
            int lastDotIndex = cleanUrl.lastIndexOf('.');
            
            if (lastDotIndex != -1 && lastDotIndex < cleanUrl.length() - 1) {
                return cleanUrl.substring(lastDotIndex + 1).toLowerCase();
            }
            
            return null;
        } catch (Exception e) {
            log.warn("파일 확장자 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 예시 사용법을 보여주는 메서드
     */
    public static void main(String[] args) {
        String presignedUrl = "https://ckokservice.s3.ap-northeast-2.amazonaws.com/banners/banner_1753967233913_b6fba553.jpeg?x-amz-algorithm=aws4-hmac-sha256&x-amz-date=20250731t130711z&x-amz-signedheaders=content-type%3bhost&x-amz-credential=akiawa656tqwfact4sgo%2f20250731%2fap-northeast-2%2fs3%2faws4_request&x-amz-expires=300&x-amz-signature=1483466b13813464dff95bf7782e4f7349cdfd442d0668683a4e5e206cb39104";
        
        System.out.println("원본 URL: " + presignedUrl);
        System.out.println("정리된 URL: " + cleanS3Url(presignedUrl));
        System.out.println("버킷명: " + extractBucketName(presignedUrl));
        System.out.println("객체 키: " + extractObjectKey(presignedUrl));
        System.out.println("파일 확장자: " + extractFileExtension(presignedUrl));
        System.out.println("유효한 S3 URL: " + isValidS3Url(presignedUrl));
    }
}
