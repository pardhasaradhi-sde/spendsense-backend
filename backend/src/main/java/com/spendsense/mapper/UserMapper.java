package com.spendsense.mapper;

import com.spendsense.dto.response.UserResponse;
import com.spendsense.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
