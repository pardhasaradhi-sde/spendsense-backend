package com.spendsense.mapper;

import com.spendsense.dto.request.CreateTransactionRequest;
import com.spendsense.dto.response.TransactionResponse;
import com.spendsense.model.Transaction;
import com.spendsense.model.enums.TransactionStatus;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {
        Transaction transaction=new Transaction();
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setCategory(request.getCategory());
        transaction.setReceiptUrl(request.getReceiptUrl());
        transaction.setIsRecurring(request.getIsRecurring()!=null?
                request.getIsRecurring():false);
        transaction.setRecurringInterval(request.getRecurringInterval());
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .category(transaction.getCategory())
                .receiptUrl(transaction.getReceiptUrl())
                .isRecurring(transaction.getIsRecurring())
                .recurringInterval(transaction.getRecurringInterval())
                .status(transaction.getStatus())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
