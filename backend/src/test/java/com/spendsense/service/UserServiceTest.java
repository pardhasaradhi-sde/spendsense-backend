package com.spendsense.service;

import com.spendsense.dto.response.UserResponse;
import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.mapper.UserMapper;
import com.spendsense.model.User;
import com.spendsense.model.enums.UserRole;
import com.spendsense.model.webhook.ClerkUserData;
import com.spendsense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private ClerkUserData clerkUserData;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setClerkUserId("clerk_123");
        testUser.setEmail("test@example.com");
        testUser.setName("John Doe");
        testUser.setImageUrl("https://example.com/image.jpg");
        testUser.setRole(UserRole.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        clerkUserData = new ClerkUserData();
        clerkUserData.setId("clerk_123");
        clerkUserData.setFirstName("John");
        clerkUserData.setLastName("Doe");
        clerkUserData.setImageUrl("https://example.com/image.jpg");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setEmail("test@example.com");
        userResponse.setName("John Doe");
    }

    @Test
    void createUserFromClerk_WhenUserDoesNotExist_ShouldCreateNewUser() {
        // Arrange
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUserFromClerk(clerkUserData);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findByClerkUserId("clerk_123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserFromClerk_WhenUserExists_ShouldReturnExistingUser() {
        // Arrange
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.createUserFromClerk(clerkUserData);

        // Assert
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByClerkUserId("clerk_123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserFromClerk_WhenUserExists_ShouldUpdateUser() {
        // Arrange
        clerkUserData.setFirstName("Jane");
        clerkUserData.setLastName("Smith");
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUserFromClerk(clerkUserData);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Smith");
        verify(userRepository).findByClerkUserId("clerk_123");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserFromClerk_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUserFromClerk(clerkUserData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUserFromClerk_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUserFromClerk("clerk_123");

        // Assert
        verify(userRepository).findByClerkUserId("clerk_123");
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserFromClerk_WhenUserDoesNotExist_ShouldLogWarning() {
        // Arrange
        when(userRepository.findByClerkUserId("clerk_123")).thenReturn(Optional.empty());

        // Act
        userService.deleteUserFromClerk("clerk_123");

        // Assert
        verify(userRepository).findByClerkUserId("clerk_123");
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getCurrentUser(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John Doe");
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getCurrentUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // Act
        List<UserResponse> result = userService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void updateRole_WhenUserExists_ShouldUpdateUserRole() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateRole(userId, UserRole.ADMIN);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateRole_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateRole(userId, UserRole.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User Not found");
    }

    @Test
    void delete_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    void delete_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User Not found");
    }
}
