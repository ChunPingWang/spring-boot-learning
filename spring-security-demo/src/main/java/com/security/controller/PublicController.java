package com.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公開控制器
 *
 * 不需要認證即可存取的端點
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "公開", description = "公開 API（無需認證）")
public class PublicController {

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    @Operation(summary = "健康檢查", description = "檢查 API 是否正常運作")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Spring Security Demo 正常運作中"
        ));
    }

    /**
     * API 資訊
     */
    @GetMapping("/info")
    @Operation(summary = "API 資訊", description = "獲取 API 的基本資訊")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        return ResponseEntity.ok(Map.of(
            "name", "Spring Security Demo",
            "version", "1.0.0",
            "description", "Spring Security JWT 認證示範",
            "features", Map.of(
                "authentication", "JWT Token",
                "authorization", "角色基礎存取控制 (RBAC)",
                "passwordEncoding", "BCrypt"
            )
        ));
    }
}
