package com.example.adminservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Swagger UI IP 접근 제한 설정
 */
@Slf4j
@Configuration
public class SwaggerAccessConfig {

    @Value("${swagger.allowed-ips:127.0.0.1,::1,0:0:0:0:0:0:0:1}")
    private String allowedIps;

    @Value("${swagger.access.enabled:true}")
    private boolean swaggerAccessEnabled;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * Swagger 전용 보안 필터 체인
     */
    @Bean
    @Order(0) // AdminSecurityConfig보다 먼저 실행
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Swagger 보안 필터 체인 구성 중 - 프로필: {}, 접근허용: {}", activeProfile, swaggerAccessEnabled);

        return http
                .securityMatcher(swaggerRequestMatcher())
                .authorizeHttpRequests(authorize -> {
                    if (!swaggerAccessEnabled) {
                        log.warn("Swagger 접근이 비활성화되어 있습니다");
                        authorize.anyRequest().denyAll();
                    } else if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
                        log.info("프로덕션 환경 - IP 제한 적용: {}", allowedIps);
                        authorize.anyRequest().access(new IpAddressAuthorizationManager(getAllowedIpList()));
                    } else {
                        log.info("개발 환경 - Swagger 전체 접근 허용");
                        authorize.anyRequest().permitAll();
                    }
                })
                .exceptionHandling(exceptions -> 
                    exceptions.accessDeniedHandler(swaggerAccessDeniedHandler())
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }

    /**
     * Swagger 관련 요청 매처
     */
    private RequestMatcher swaggerRequestMatcher() {
        return request -> {
            String uri = request.getRequestURI();
            return uri.contains("/swagger-ui") || 
                   uri.contains("/api-docs") || 
                   uri.contains("/v3/api-docs") ||
                   uri.equals("/swagger-ui.html");
        };
    }

    /**
     * IP 주소 기반 접근 제어 매니저
     */
    public static class IpAddressAuthorizationManager implements org.springframework.security.authorization.AuthorizationManager<org.springframework.security.web.access.intercept.RequestAuthorizationContext> {
        
        private final List<String> allowedIps;

        public IpAddressAuthorizationManager(List<String> allowedIps) {
            this.allowedIps = allowedIps;
        }

        @Override
        public org.springframework.security.authorization.AuthorizationDecision check(
                java.util.function.Supplier<org.springframework.security.core.Authentication> authentication,
                org.springframework.security.web.access.intercept.RequestAuthorizationContext context) {

            HttpServletRequest request = context.getRequest();
            String clientIp = getClientIpAddress(request);
            
            log.debug("Swagger 접근 시도 - Client IP: {}, 허용된 IP 목록: {}", clientIp, allowedIps);

            boolean allowed = allowedIps.contains(clientIp) || 
                            allowedIps.contains("0.0.0.0") || 
                            isLocalhost(clientIp);

            if (allowed) {
                log.info("Swagger 접근 허용 - IP: {}", clientIp);
                return new org.springframework.security.authorization.AuthorizationDecision(true);
            } else {
                log.warn("Swagger 접근 거부 - IP: {}, 허용된 IP가 아닙니다", clientIp);
                return new org.springframework.security.authorization.AuthorizationDecision(false);
            }
        }

        /**
         * 실제 클라이언트 IP 주소 추출 (프록시, 로드밸런서 고려)
         */
        private String getClientIpAddress(HttpServletRequest request) {
            String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP", 
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED"
            };

            for (String header : headers) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    // X-Forwarded-For는 여러 IP를 콤마로 구분할 수 있음
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    return ip;
                }
            }

            return request.getRemoteAddr();
        }

        /**
         * 로컬호스트 주소인지 확인
         */
        private boolean isLocalhost(String ip) {
            return "127.0.0.1".equals(ip) || 
                   "0:0:0:0:0:0:0:1".equals(ip) || 
                   "::1".equals(ip) ||
                   "localhost".equals(ip);
        }
    }

    /**
     * Swagger 접근 거부 핸들러
     */
    private AccessDeniedHandler swaggerAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String clientIp = getClientIpAddress(request);
            log.warn("Swagger 접근 거부됨 - IP: {}, URI: {}", clientIp, request.getRequestURI());
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            
            String jsonResponse = String.format("""
                {
                    "error": "Access Denied",
                    "message": "Swagger UI에 대한 접근이 제한되었습니다",
                    "details": "허용되지 않은 IP 주소입니다: %s",
                    "timestamp": "%s"
                }
                """, clientIp, java.time.LocalDateTime.now());
            
            response.getWriter().write(jsonResponse);
        };
    }

    /**
     * 허용된 IP 목록 파싱
     */
    private List<String> getAllowedIpList() {
        return Arrays.stream(allowedIps.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .toList();
    }

    /**
     * 실제 클라이언트 IP 주소 추출 (유틸리티 메소드)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", 
            "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
