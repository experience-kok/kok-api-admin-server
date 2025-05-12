package com.example.adminservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AdminSwaggerConfig {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("CKOK 관리자 서비스 API")
                .version("v1.0")
                .description("CKOK 관리자 서비스 API 문서")
                .contact(new Contact().name("CKOK Admin").url("https://admin.ckok.kr"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // 서버 URL 설정
        List<Server> servers = getServers();

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addSecurityItem(securityRequirement)
                .servers(servers);
    }

    private List<Server> getServers() {
        // 서버 URL, 컨텍스트 경로가 /admin에서 /admin-api로 변경됨
        Server prodServer = new Server()
                .url("https://admin.ckok.kr/admin-api")
                .description("운영 서버");
                
        Server localServer = new Server()
                .url("http://localhost:8081/admin-api")
                .description("로컬 서버");

        return Arrays.asList(prodServer, localServer);
    }
}
