package com.example.adminservice.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 허용할 오리진 목록을 상수로 정의
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            // Development origins
            "http://localhost:3000",
            "https://localhost:3000",
            "http://localhost:3001",
            "https://localhost:3001",
            // Production origins
            "http://chkok.kr",
            "https://chkok.kr",
            "http://admin.chkok.kr",
            "https://admin.chkok.kr",
            "http://api.chkok.kr",
            "https://api.chkok.kr"
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers",
                        "Cache-Control",
                        "Content-Length",
                        "x-amz-content-sha256",
                        "x-amz-date"
                )
                .allowCredentials(true)
                .exposedHeaders("Authorization", "Content-Disposition")
                .maxAge(3600); // 1시간 캐싱
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 파일 서빙을 위한 리소스 핸들러 추가
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // 1시간 캐싱

        // 배너 이미지 서빙을 위한 리소스 핸들러 추가
        registry.addResourceHandler("/files/banners/**")
                .addResourceLocations("file:uploads/banners/")
                .setCachePeriod(3600); // 1시간 캐싱

        // 정적 리소스 핸들러 추가 (HTML, CSS, JS 등)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600) // 1시간 캐싱
                .resourceChain(true);
    }

    /**
     * 파일 업로드 설정
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 파일 하나의 최대 크기 (5MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(5));

        // 전체 요청의 최대 크기 (10MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));

        // 임시 파일 저장 경로
        factory.setLocation(System.getProperty("java.io.tmpdir"));

        return factory.createMultipartConfig();
    }
}
