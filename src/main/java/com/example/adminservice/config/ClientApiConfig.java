package com.example.adminservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 클라이언트 API 서버와의 통신을 위한 설정
 */
@Configuration
public class ClientApiConfig {

    @Value("${client.api.base-url:http://localhost:8080}")
    private String clientApiBaseUrl;

    @Value("${client.api.notification.timeout:30}")
    private int notificationTimeoutSeconds;

    @Bean(name = "clientApiWebClient")
    public WebClient clientApiWebClient() {
        // HTTP 클라이언트 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(notificationTimeoutSeconds)) // 응답 타임아웃
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(notificationTimeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(clientApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "AdminService/1.0")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1 * 1024 * 1024)) // 1MB
                .build();
    }
}
