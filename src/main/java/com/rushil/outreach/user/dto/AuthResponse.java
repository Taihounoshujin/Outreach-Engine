package com.rushil.outreach.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;   // Always "Bearer"
    private UserResponse user;
}