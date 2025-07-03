package com.example.adminservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AdminSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("관리자 서비스 API")
                .version("v1.0")
                .description("관리자 서비스 API 문서")
                .contact(new Contact().name("개발팀").email("dev@example.com"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // 서버 URL 명시적 설정
        List<Server> servers = Arrays.asList(
            new Server().url("https://chkok.kr/admin-api").description("운영 서버 (HTTPS)"),
            new Server().url("http://localhost:8081/admin-api").description("로컬 서버")
        );

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addSecurityItem(securityRequirement)
                .servers(servers);
    }
}
