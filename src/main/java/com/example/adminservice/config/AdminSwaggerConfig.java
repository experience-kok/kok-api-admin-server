package com.example.adminservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger OpenAPI 설정
 * 환경별로 다른 설정을 적용하며, IP 접근 제한과 연동됩니다.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    value = "swagger.production.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class AdminSwaggerConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Value("${swagger.access.enabled:true}")
    private boolean swaggerAccessEnabled;

    @Bean
    public OpenAPI openAPI() {
        log.info("Swagger OpenAPI 구성 중 - Profile: {}, 접근허용: {}", activeProfile, swaggerAccessEnabled);

        Info info = new Info()
                .title("관리자 서비스 API")
                .version("v1.0")
                .description(getApiDescription())
                .contact(new Contact().name("개발팀").email("dev@example.com"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // 환경별 서버 URL 설정
        List<Server> servers = getServersByProfile();

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addSecurityItem(securityRequirement)
                .servers(servers);

        log.info("Swagger OpenAPI 구성 완료 - 서버 목록: {}", servers.size());
        return openAPI;
    }

    /**
     * 환경별 API 설명 생성
     */
    private String getApiDescription() {
        StringBuilder description = new StringBuilder();
        description.append("관리자 서비스 API 문서\n\n");
        
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            description.append(" **운영 환경** - IP 접근 제한이 적용됩니다.\n");
            description.append(" 인증된 관리자만 접근 가능합니다.\n\n");
        } else {
            description.append("🔧 **개발 환경** - 테스트 및 개발용입니다.\n\n");
        }
        
        description.append("### 주요 기능\n");
        description.append("-  **배너 관리**: 웹사이트 배너 이미지 관리\n");
        description.append("-  **캠페인 승인**: 마케팅 캠페인 검토 및 승인\n");
        description.append("- **사용자 관리**: 회원 정보 조회 및 관리\n");
        description.append("- **알림 관리**: 시스템 알림 발송 및 관리\n");
        description.append("- **이미지 업로드**: S3 기반 이미지 관리\n\n");
        
        description.append("### 인증 방법\n");
        description.append("1. `/auth/login` 엔드포인트로 로그인\n");
        description.append("2. 받은 JWT 토큰을 `Authorization: Bearer {token}` 헤더에 포함\n");
        description.append("3. API 호출 시 토큰 전송\n");

        return description.toString();
    }

    /**
     * 환경별 서버 URL 설정
     */
    private List<Server> getServersByProfile() {
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            return Arrays.asList(
                new Server()
                    .url("https://chkok.kr/admin-api")
                    .description("🔒 운영 서버 (HTTPS, IP 제한 적용)"),
                new Server()
                    .url("https://admin.chkok.kr/admin-api")
                    .description("🔒 관리자 서버 (HTTPS, IP 제한 적용)")
            );
        } else {
            return Arrays.asList(
                new Server()
                    .url("https://chkok.kr/admin-api")
                    .description("운영 서버")
            );
        }
    }
}
