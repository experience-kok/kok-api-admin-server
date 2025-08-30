package com.example.adminservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 클라이언트 API 서버와의 통신을 위한 설정
 */
@Configuration
public class ClientApiConfig {

    @Value("${client.api.base-url:http://localhost:8080}")
    private String clientApiBaseUrl;

    @Bean(name = "clientApiWebClient")
    public WebClient clientApiWebClient() {
        return WebClient.builder()
                .baseUrl(clientApiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
