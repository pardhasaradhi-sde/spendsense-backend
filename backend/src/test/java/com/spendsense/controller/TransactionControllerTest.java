package com.spendsense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.request.CreateTransactionRequest;
import com.spendsense.dto.request.UpdateTransactionRequest;
import com.spendsense.dto.response.TransactionResponse;
import com.spendsense.model.User;
import com.spendsense.model.enums.TransactionType;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserPrincipal userPrincipal;

    @MockBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private TransactionResponse transactionResponse;
    private CreateTransactionRequest createRequest;
    private UpdateTransactionRequest updateRequest;
    private UUID userId;
    private UUID accountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        transactionResponse = new TransactionResponse();
        transactionResponse.setId(transactionId);
        transactionResponse.setType(TransactionType.INCOME);
        transactionResponse.setAmount(BigDecimal.valueOf(500.00));
        transactionResponse.setDescription("Salary");

        createRequest = new CreateTransactionRequest();
        createRequest.setAccountId(accountId);
        createRequest.setType(TransactionType.INCOME);
        createRequest.setAmount(BigDecimal.valueOf(500.00));
        createRequest.setDescription("Salary");
        createRequest.setDate(LocalDateTime.now());

        updateRequest = new UpdateTransactionRequest();
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setAmount(BigDecimal.valueOf(300.00));
        updateRequest.setDescription("Groceries");
    }

    @Test
    @WithMockUser
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(transactionService.createTransaction(eq(userId), any(CreateTransactionRequest.class)))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Salary"));

        verify(transactionService).createTransaction(eq(userId), any(CreateTransactionRequest.class));
    }

    @Test
    @WithMockUser
    void getUserTransactions_ShouldReturnPagedTransactions() throws Exception {
        // Arrange
        Page<TransactionResponse> page = new PageImpl<>(Arrays.asList(transactionResponse),
                PageRequest.of(0, 5), 1);
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(transactionService.getUserTransactions(eq(userId), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                        .param("page", "0")
                        .param("size", "5")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()))
                .andExpect(jsonPath("$.content[0].description").value("Salary"));

        verify(transactionService).getUserTransactions(eq(userId), any());
    }

    @Test
    @WithMockUser
    void getAccountTransactions_ShouldReturnPagedTransactions() throws Exception {
        // Arrange
        Page<TransactionResponse> page = new PageImpl<>(Arrays.asList(transactionResponse),
                PageRequest.of(0, 5), 1);
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(transactionService.getAccountTransactions(eq(userId), eq(accountId), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/transactions/account/{accountId}", accountId)
                        .param("page", "0")
                        .param("size", "5")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()));

        verify(transactionService).getAccountTransactions(eq(userId), eq(accountId), any());
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturnTransaction() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(transactionService.getTransaction(userId, transactionId)).thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.description").value("Salary"));

        verify(transactionService).getTransaction(userId, transactionId);
    }

    @Test
    @WithMockUser
    void updateTransaction_ShouldReturnUpdatedTransaction() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(transactionService.updateTransaction(eq(userId), eq(transactionId), any(UpdateTransactionRequest.class)))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()));

        verify(transactionService).updateTransaction(eq(userId), eq(transactionId), any(UpdateTransactionRequest.class));
    }

    @Test
    @WithMockUser
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(userId, transactionId);
    }
}
