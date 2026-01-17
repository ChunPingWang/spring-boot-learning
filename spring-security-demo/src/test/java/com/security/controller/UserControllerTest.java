package com.security.controller;

import com.security.entity.Role;
import com.security.entity.User;
import com.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用者控制器測試
 *
 * 展示 Spring Security Test 的使用方式
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String moderatorToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // 為不同角色建立 Token
        User admin = User.builder()
            .username("admin")
            .email("admin@example.com")
            .password("encoded")
            .build();
        admin.addRole(Role.ROLE_USER);
        admin.addRole(Role.ROLE_ADMIN);
        adminToken = jwtService.generateToken(admin);

        User moderator = User.builder()
            .username("moderator")
            .email("moderator@example.com")
            .password("encoded")
            .build();
        moderator.addRole(Role.ROLE_USER);
        moderator.addRole(Role.ROLE_MODERATOR);
        moderatorToken = jwtService.generateToken(moderator);

        User user = User.builder()
            .username("user")
            .email("user@example.com")
            .password("encoded")
            .build();
        user.addRole(Role.ROLE_USER);
        userToken = jwtService.generateToken(user);
    }

    @Nested
    @DisplayName("獲取當前使用者資訊")
    class GetCurrentUserTests {

        @Test
        @DisplayName("已認證使用者可以獲取自己的資訊")
        void getCurrentUserAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me")
                    .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
        }

        @Test
        @DisplayName("未認證使用者被拒絕存取")
        void getCurrentUserUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("無效 Token 被拒絕")
        void getCurrentUserInvalidToken() throws Exception {
            mockMvc.perform(get("/api/users/me")
                    .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("管理員端點存取控制")
    class AdminEndpointTests {

        @Test
        @DisplayName("管理員可以存取管理員端點")
        void adminCanAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/admin-only")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("管理員")));
        }

        @Test
        @DisplayName("一般使用者無法存取管理員端點")
        void userCannotAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/admin-only")
                    .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("版主無法存取管理員端點")
        void moderatorCannotAccessAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/admin-only")
                    .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("版主端點存取控制")
    class ModeratorEndpointTests {

        @Test
        @DisplayName("管理員可以存取版主端點")
        void adminCanAccessModeratorEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/moderator")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("版主可以存取版主端點")
        void moderatorCanAccessModeratorEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/moderator")
                    .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("一般使用者無法存取版主端點")
        void userCannotAccessModeratorEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/moderator")
                    .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        }
    }
}
