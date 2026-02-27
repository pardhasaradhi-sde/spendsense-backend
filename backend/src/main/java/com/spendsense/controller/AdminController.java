package com.spendsense.controller;

import com.spendsense.dto.response.UserResponse;
import com.spendsense.mapper.UserMapper;
import com.spendsense.model.User;
import com.spendsense.model.enums.UserRole;
import com.spendsense.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID id,
            @RequestParam UserRole role) {
        User user = userService.updateRole(id, role);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
