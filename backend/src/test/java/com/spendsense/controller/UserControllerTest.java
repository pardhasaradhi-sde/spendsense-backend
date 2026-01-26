package com.spendsense.controller;

import com.spendsense.dto.response.UserResponse;
import com.spendsense.model.User;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserPrincipal userPrincipal;

    @MockBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("John Doe");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setEmail("test@example.com");
        userResponse.setName("John Doe");
    }

    @Test
    @WithMockUser
    void getCurrentUser_ShouldReturnUserResponse() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(userService.getCurrentUser(userId)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
