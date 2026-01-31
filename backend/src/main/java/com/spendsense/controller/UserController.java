package com.spendsense.controller;

import com.spendsense.dto.response.UserResponse;
import com.spendsense.model.User;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;
    private final UserPrincipal userPrincipal;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the authenticated user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        User user = userPrincipal.getCurrentUser(authentication);
        UserResponse response = userService.getCurrentUser(user.getId());
        return ResponseEntity.ok(response);
    }
}
