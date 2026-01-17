package com.security.config;

import com.security.service.JwtService;
import com.security.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JWT 認證過濾器
 *
 * 每個請求都會經過此過濾器
 * 驗證 JWT Token 並設置 SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 從請求標頭取得 Authorization
        final String authHeader = request.getHeader("Authorization");

        // 檢查是否有 Bearer Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取 Token（去掉 "Bearer " 前綴）
            final String jwt = authHeader.substring(7);

            // 從 Token 提取使用者名稱
            final String username = jwtService.extractUsername(jwt);

            // 如果使用者名稱存在且尚未認證
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 載入使用者詳情
                UserDetails userDetails = userService.loadUserByUsername(username);

                // 驗證 Token 是否有效
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 建立認證 Token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    // 設置認證詳情
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 將認證資訊存入 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token 無效或解析失敗時，記錄日誌並繼續處理（不設置認證）
            log.debug("JWT Token 驗證失敗: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
