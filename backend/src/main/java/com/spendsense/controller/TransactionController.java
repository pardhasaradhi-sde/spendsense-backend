package com.spendsense.controller;

import com.spendsense.dto.request.CreateTransactionRequest;
import com.spendsense.dto.request.UpdateTransactionRequest;
import com.spendsense.dto.response.TransactionResponse;
import com.spendsense.model.User;
import com.spendsense.security.UserPrincipal;
import com.spendsense.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management APIs")
public class TransactionController {
    private final TransactionService  transactionService;
    private final UserPrincipal userPrincipal;

    @PostMapping()
    @Operation(summary = "Create a new transaction", description = "Creates a new income or expense transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CreateTransactionRequest request
    ){
        User user=userPrincipal.getCurrentUser(authentication);
        TransactionResponse response =transactionService.createTransaction(user.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping
    @Operation(summary = "Get user transactions", description = "Retrieves all transactions for the authenticated user with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "desc") String direction){
        User user=userPrincipal.getCurrentUser(authentication);
        Sort sort=direction.equalsIgnoreCase("asc")
                ?Sort.by("date").ascending()
                :Sort.by("date").descending();
        Pageable pageable = PageRequest.of(page, size,sort);

        Page<TransactionResponse> transactions = transactionService.getUserTransactions(user.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account transactions", description = "Retrieves all transactions for a specific account with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactions(
        @Parameter(hidden = true) Authentication authentication,
        @PathVariable UUID accountId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "desc") String direction
    ){
        User user=userPrincipal.getCurrentUser(authentication);
        Sort sort=direction.equalsIgnoreCase("asc")
                ?Sort.by("date").ascending()
                :Sort.by("date").descending();
        Pageable pageable = PageRequest.of(page, size,sort);
        Page<TransactionResponse> transactions=transactionService.getAccountTransactions(user.getId(),accountId,pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a single transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable UUID id
    ){
        User user=userPrincipal.getCurrentUser(authentication);
        TransactionResponse response =transactionService.getTransaction(user.getId(),id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates an existing transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ){
        User user=userPrincipal.getCurrentUser(authentication);
        TransactionResponse updated =transactionService.updateTransaction(user.getId(),id,request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Deletes a transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable UUID id
    ){
        User user=userPrincipal.getCurrentUser(authentication);
        transactionService.deleteTransaction(user.getId(),id);
        return ResponseEntity.noContent().build();
    }

}
