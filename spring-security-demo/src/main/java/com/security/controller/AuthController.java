package com.security.controller;

import com.security.dto.AuthResponse;
import com.security.dto.LoginRequest;
import com.security.dto.RegisterRequest;
import com.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證控制器
 *
 * 處理使用者註冊和登入
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "認證", description = "使用者認證相關 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 使用者註冊
     */
    @PostMapping("/register")
    @Operation(summary = "使用者註冊", description = "建立新使用者帳號並返回 JWT Token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 使用者登入
     */
    @PostMapping("/login")
    @Operation(summary = "使用者登入", description = "驗證使用者憑證並返回 JWT Token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
