package com.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 設定
 *
 * 配置 JWT 認證、授權規則、CORS、CSRF 等安全機制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 啟用方法級安全（@PreAuthorize, @Secured 等）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * 安全過濾鏈配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 停用 CSRF（使用 JWT 時通常不需要）
            .csrf(AbstractHttpConfigurer::disable)

            // 授權規則
            .authorizeHttpRequests(auth -> auth
                // 公開端點
                .requestMatchers(
                    "/api/auth/**",           // 認證相關
                    "/api/public/**",         // 公開 API
                    "/swagger-ui/**",         // Swagger UI
                    "/swagger-ui.html",
                    "/v3/api-docs/**",        // OpenAPI 文檔
                    "/h2-console/**"          // H2 控制台
                ).permitAll()

                // 管理員端點
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 其他請求需要認證
                .anyRequest().authenticated()
            )

            // Session 管理（使用 JWT 時為無狀態）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 認證提供者
            .authenticationProvider(authenticationProvider())

            // 在 UsernamePasswordAuthenticationFilter 之前加入 JWT 過濾器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // H2 Console 需要設置 frame options（開發用）
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * 認證提供者
     * 使用 DaoAuthenticationProvider 進行資料庫認證
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 認證管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 密碼編碼器
     * 使用 BCrypt 進行密碼雜湊
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
