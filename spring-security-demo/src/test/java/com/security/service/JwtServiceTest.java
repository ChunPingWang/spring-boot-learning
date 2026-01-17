package com.security.service;

import com.security.entity.Role;
import com.security.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 服務測試
 */
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("生成並驗證 JWT Token")
    void generateAndValidateToken() {
        // Given
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .build();
        user.addRole(Role.ROLE_USER);

        // When
        String token = jwtService.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("提取使用者名稱")
    void extractUsername() {
        // Given
        User user = User.builder()
            .username("john")
            .email("john@example.com")
            .password("password")
            .build();

        String token = jwtService.generateToken(user);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("john");
    }

    @Test
    @DisplayName("Token 對不同使用者無效")
    void tokenInvalidForDifferentUser() {
        // Given
        User user1 = User.builder()
            .username("user1")
            .email("user1@example.com")
            .password("password")
            .build();

        User user2 = User.builder()
            .username("user2")
            .email("user2@example.com")
            .password("password")
            .build();

        String token = jwtService.generateToken(user1);

        // When & Then
        assertThat(jwtService.isTokenValid(token, user2)).isFalse();
    }
}
