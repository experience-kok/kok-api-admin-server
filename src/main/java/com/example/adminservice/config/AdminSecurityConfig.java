package com.example.adminservice.config;

import com.example.adminservice.security.AdminJwtAuthenticationFilter;
import com.example.adminservice.security.AdminJwtAuthorizationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@Order(1)
public class AdminSecurityConfig {

    // 서버 URL 설정
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:3000",
        "https://localhost:3000",
        "http://chkok.kr",
        "https://chkok.kr",
        "https://www.chkok.kr"
    );

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        log.info("AdminSecurityConfig - 보안 필터 체인 구성 중");
        
        // JWT 인증 필터 생성
        AdminJwtAuthenticationFilter authenticationFilter = new AdminJwtAuthenticationFilter(authenticationManager);
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            // 컨텍스트 경로(/admin)를 고려한 매핑
            .securityMatcher("/**") 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> {
                log.info("URL 권한 구성 중");
                authorize
                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers("/auth/refresh").permitAll()
                    .requestMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/*.html", "/static/**").permitAll() 
                    .requestMatchers("/images/**").permitAll() // placeholder 이미지
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().hasRole("ADMIN");
            })
            .addFilter(authenticationFilter)
            .addFilter(new AdminJwtAuthorizationFilter(authenticationManager));
        
        log.info("보안 필터 체인 구성 완료");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 해시 알고리즘을 사용하여 안전하게 비밀번호 암호화
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 개발/운영 환경 모든 오리진 허용 (실제 운영에서는 특정 도메인만 허용)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 모든 HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 프리플라이트 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
