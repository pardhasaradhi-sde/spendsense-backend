package com.spendsense.controller;

import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.request.UpdateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.model.User;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management APIs")
public class AccountController {
        private final AccountService accountService;
        private final UserPrincipal userPrincipal;

        @PostMapping
        @Operation(summary = "Create a new account", description = "Creates a new financial account for the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Account created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<AccountResponse> createAccount(
                        Authentication authentication,
                        @Valid @RequestBody CreateAccountRequest request) {
                User user = userPrincipal.getCurrentUser(authentication);
                AccountResponse response = accountService.createAccount(user.getId(), request);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @GetMapping
        @Operation(summary = "Get user accounts", description = "Retrieves all accounts for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<List<AccountResponse>> getUserAccounts(
                        Authentication authentication) {
                User user = userPrincipal.getCurrentUser(authentication);
                List<AccountResponse> accounts = accountService.getUserAccounts(user.getId());
                return new ResponseEntity<>(accounts, HttpStatus.OK);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get account by ID", description = "Retrieves a single account by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        public ResponseEntity<AccountResponse> getAccount(
                        Authentication authentication,
                        @PathVariable UUID id) {
                User user = userPrincipal.getCurrentUser(authentication);
                AccountResponse response = accountService.getAccountById(user.getId(), id);
                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update account", description = "Updates an existing account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        public ResponseEntity<AccountResponse> updateAccount(
                        Authentication authentication,
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateAccountRequest request) {
                User user = userPrincipal.getCurrentUser(authentication);
                AccountResponse response = accountService.updateAccount(user.getId(), id, request);
                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete account", description = "Deletes an account by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        public ResponseEntity<Void> deleteAccount(
                        Authentication authentication,
                        @PathVariable UUID id) {
                User user = userPrincipal.getCurrentUser(authentication);
                accountService.deleteAccount(user.getId(), id);
                return ResponseEntity.noContent().build();
        }
}
