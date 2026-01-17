package com.security.service;

import com.security.dto.AuthResponse;
import com.security.dto.LoginRequest;
import com.security.dto.RegisterRequest;
import com.security.entity.Role;
import com.security.entity.User;
import com.security.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 認證服務
 *
 * 處理使用者註冊、登入等認證相關邏輯
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 使用者註冊
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 檢查使用者名稱是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("使用者名稱已存在: " + request.getUsername());
        }

        // 檢查電子郵件是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("電子郵件已存在: " + request.getEmail());
        }

        // 建立新使用者
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .build();

        // 預設角色為 USER
        user.addRole(Role.ROLE_USER);

        // 儲存使用者
        User savedUser = userService.save(user);

        // 生成 JWT Token
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.of(
            token,
            jwtService.getExpirationTime(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList())
        );
    }

    /**
     * 使用者登入
     */
    public AuthResponse login(LoginRequest request) {
        // 使用 AuthenticationManager 進行認證
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // 認證成功，獲取使用者資訊
        User user = (User) userService.loadUserByUsername(request.getUsername());

        // 生成 JWT Token
        String token = jwtService.generateToken(user);

        return AuthResponse.of(
            token,
            jwtService.getExpirationTime(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList())
        );
    }
}
