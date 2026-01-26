package com.spendsense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.request.UpdateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.model.User;
import com.spendsense.model.enums.AccountType;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserPrincipal userPrincipal;

    @MockBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private AccountResponse accountResponse;
    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        accountResponse = new AccountResponse();
        accountResponse.setId(accountId);
        accountResponse.setName("Checking Account");
        accountResponse.setType(AccountType.CHECKING);
        accountResponse.setBalance(BigDecimal.valueOf(1000.00));

        createRequest = new CreateAccountRequest();
        createRequest.setName("Checking Account");
        createRequest.setType(AccountType.CHECKING);
        createRequest.setBalance(BigDecimal.valueOf(1000.00));
        createRequest.setIsDefault(false);

        updateRequest = new UpdateAccountRequest();
        updateRequest.setName("Updated Account");
        updateRequest.setType(AccountType.SAVINGS);
        updateRequest.setBalance(BigDecimal.valueOf(2000.00));
    }

    @Test
    @WithMockUser
    void createAccount_ShouldReturnCreatedAccount() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(accountService.createAccount(eq(userId), any(CreateAccountRequest.class)))
                .thenReturn(accountResponse);

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Checking Account"))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(accountService).createAccount(eq(userId), any(CreateAccountRequest.class));
    }

    @Test
    @WithMockUser
    void getUserAccounts_ShouldReturnAllAccounts() throws Exception {
        // Arrange
        List<AccountResponse> accounts = Arrays.asList(accountResponse);
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(accountService.getUserAccounts(userId)).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountId.toString()))
                .andExpect(jsonPath("$[0].name").value("Checking Account"));

        verify(accountService).getUserAccounts(userId);
    }

    @Test
    @WithMockUser
    void getAccount_ShouldReturnAccount() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(accountService.getAccountById(userId, accountId)).thenReturn(accountResponse);

        // Act & Assert
        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Checking Account"));

        verify(accountService).getAccountById(userId, accountId);
    }

    @Test
    @WithMockUser
    void updateAccount_ShouldReturnUpdatedAccount() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(accountService.updateAccount(eq(userId), eq(accountId), any(UpdateAccountRequest.class)))
                .thenReturn(accountResponse);

        // Act & Assert
        mockMvc.perform(put("/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()));

        verify(accountService).updateAccount(eq(userId), eq(accountId), any(UpdateAccountRequest.class));
    }

    @Test
    @WithMockUser
    void deleteAccount_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/accounts/{id}", accountId))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(userId, accountId);
    }
}
