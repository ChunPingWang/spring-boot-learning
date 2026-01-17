package com.security.controller;

import com.security.dto.UserDTO;
import com.security.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用者控制器
 *
 * 展示受保護的端點和方法級安全
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "使用者", description = "使用者相關 API（需要認證）")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    /**
     * 獲取當前使用者資訊
     *
     * @AuthenticationPrincipal 可以直接注入當前認證的使用者
     */
    @GetMapping("/me")
    @Operation(summary = "獲取當前使用者", description = "返回當前已認證使用者的資訊")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    /**
     * 展示 @PreAuthorize 的使用
     * 只有 ADMIN 角色可以存取
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "管理員專用", description = "只有管理員角色可以存取此端點")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("歡迎，管理員！這是受保護的管理員資源。");
    }

    /**
     * 展示 @PreAuthorize 的複雜表達式
     * ADMIN 或 MODERATOR 角色可以存取
     */
    @GetMapping("/moderator")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "管理者專用", description = "管理員或版主角色可以存取此端點")
    public ResponseEntity<String> moderatorAccess() {
        return ResponseEntity.ok("歡迎，管理者！這是管理者資源。");
    }
}
