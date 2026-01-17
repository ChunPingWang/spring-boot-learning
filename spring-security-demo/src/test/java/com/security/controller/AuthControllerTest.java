package com.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.dto.LoginRequest;
import com.security.dto.RegisterRequest;
import com.security.entity.Role;
import com.security.entity.User;
import com.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認證控制器整合測試
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 清理非預設使用者
        userRepository.findAll().stream()
            .filter(u -> !u.getUsername().equals("admin")
                && !u.getUsername().equals("moderator")
                && !u.getUsername().equals("user"))
            .forEach(u -> userRepository.delete(u));
    }

    @Nested
    @DisplayName("註冊測試")
    class RegisterTests {

        @Test
        @DisplayName("成功註冊新使用者")
        void registerSuccess() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .fullName("新使用者")
                .build();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));
        }

        @Test
        @DisplayName("註冊失敗 - 使用者名稱已存在")
        void registerFailDuplicateUsername() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .username("admin")  // 已存在的使用者名稱
                .email("newemail@example.com")
                .password("password123")
                .build();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("使用者名稱已存在")));
        }

        @Test
        @DisplayName("註冊失敗 - 驗證錯誤")
        void registerFailValidation() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .username("ab")  // 太短
                .email("invalid-email")  // 格式錯誤
                .password("123")  // 太短
                .build();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("登入測試")
    class LoginTests {

        @Test
        @DisplayName("成功登入")
        void loginSuccess() throws Exception {
            LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles", hasItems("ROLE_USER", "ROLE_ADMIN")));
        }

        @Test
        @DisplayName("登入失敗 - 密碼錯誤")
        void loginFailWrongPassword() throws Exception {
            LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("使用者名稱或密碼錯誤")));
        }

        @Test
        @DisplayName("登入失敗 - 使用者不存在")
        void loginFailUserNotFound() throws Exception {
            LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("password123")
                .build();

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }
    }
}
