package com.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 認證回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String email;
    private List<String> roles;

    /**
     * 建立成功的認證回應
     */
    public static AuthResponse of(String accessToken, Long expiresIn,
                                  String username, String email, List<String> roles) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .username(username)
            .email(email)
            .roles(roles)
            .build();
    }
}
