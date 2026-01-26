package com.spendsense.dto.response;

import com.spendsense.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private String imageUrl;
    private UserRole role;
    private LocalDateTime createdAt;
}
