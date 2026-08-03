package com.rushil.outreach.user.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private LocalDateTime createdAt;
    private UUID id;
    private String email;
    private String displayName;
}