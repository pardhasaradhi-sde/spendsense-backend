package com.spendsense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.request.CreateBudgetRequest;
import com.spendsense.dto.request.UpdateBudgetRequest;
import com.spendsense.dto.response.BudgetResponse;
import com.spendsense.model.User;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.BudgetService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private UserPrincipal userPrincipal;

    @MockBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private BudgetResponse budgetResponse;
    private CreateBudgetRequest createRequest;
    private UpdateBudgetRequest updateRequest;
    private UUID userId;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        budgetResponse = new BudgetResponse();
        budgetResponse.setId(budgetId);
        budgetResponse.setAmount(BigDecimal.valueOf(5000.00));

        createRequest = new CreateBudgetRequest();
        createRequest.setAmount(BigDecimal.valueOf(5000.00));

        updateRequest = new UpdateBudgetRequest();
        updateRequest.setAmount(BigDecimal.valueOf(6000.00));
    }

    @Test
    @WithMockUser
    void createBudget_ShouldReturnCreatedBudget() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(budgetService.createBudget(eq(userId), any(CreateBudgetRequest.class)))
                .thenReturn(budgetResponse);

        // Act & Assert
        mockMvc.perform(post("/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.amount").value(5000.00));

        verify(budgetService).createBudget(eq(userId), any(CreateBudgetRequest.class));
    }

    @Test
    @WithMockUser
    void getBudget_ShouldReturnBudget() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(budgetService.getUserBudget(userId)).thenReturn(budgetResponse);

        // Act & Assert
        mockMvc.perform(get("/budget"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.amount").value(5000.00));

        verify(budgetService).getUserBudget(userId);
    }

    @Test
    @WithMockUser
    void updateBudget_ShouldReturnUpdatedBudget() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);
        when(budgetService.updateBudget(eq(userId), any(UpdateBudgetRequest.class)))
                .thenReturn(budgetResponse);

        // Act & Assert
        mockMvc.perform(put("/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()));

        verify(budgetService).updateBudget(eq(userId), any(UpdateBudgetRequest.class));
    }

    @Test
    @WithMockUser
    void deleteBudget_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(userPrincipal.getCurrentUser(any(Authentication.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/budget"))
                .andExpect(status().isNoContent());

        verify(budgetService).deleteBudget(userId);
    }
}
