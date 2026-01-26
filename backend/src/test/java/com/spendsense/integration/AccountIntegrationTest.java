package com.spendsense.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.model.User;
import com.spendsense.model.enums.AccountType;
import com.spendsense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setClerkUserId("test_clerk_123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser
    void accountLifecycle_CreateRetrieveUpdateDelete() throws Exception {
        // Create Account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setName("Test Checking");
        createRequest.setType(AccountType.CHECKING);
        createRequest.setBalance(BigDecimal.valueOf(5000.00));
        createRequest.setIsDefault(true);

        MvcResult createResult = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Checking"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        AccountResponse createdAccount = objectMapper.readValue(responseBody, AccountResponse.class);
        String accountId = createdAccount.getId().toString();

        // Retrieve Account
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.name").value("Test Checking"));

        // List All Accounts
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountId));

        // Update Account
        mockMvc.perform(put("/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Checking\",\"balance\":6000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Checking"));

        // Delete Account
        mockMvc.perform(delete("/accounts/" + accountId))
                .andExpect(status().isNoContent());

        // Verify Deletion
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createAccount_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create Account with null name
        CreateAccountRequest invalidRequest = new CreateAccountRequest();
        invalidRequest.setName(null);
        invalidRequest.setType(AccountType.CHECKING);
        invalidRequest.setBalance(BigDecimal.valueOf(1000.00));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
