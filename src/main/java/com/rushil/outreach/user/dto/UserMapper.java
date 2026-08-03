package com.rushil.outreach.user.dto;

import com.rushil.outreach.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private UserMapper() {} // utility class

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}