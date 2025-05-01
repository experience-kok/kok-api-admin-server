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

    // 허용할 오리진 목록을 상수로 정의
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:3000",
        "https://localhost:3000",
        "http://ckok.kr",
        "https://ckok.kr"
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
                    .anyRequest().hasRole("ADMIN");
            })
            .addFilter(authenticationFilter)
            .addFilter(new AdminJwtAuthorizationFilter(authenticationManager));
        
        log.info("보안 필터 체인 구성 완료");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 개발용: 평문 비밀번호 사용 (보안 위험이 있으므로 실제 운영 환경에서는 사용하지 마세요)
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
