package com.spendsense.model;

import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionStatus;
import com.spendsense.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false,precision =  19,scale = 2)
    private BigDecimal amount;

    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String category;

    private String receiptUrl;

    @Column(nullable = false)
    private Boolean isRecurring=false;

    @Enumerated(EnumType.STRING)
    private RecurringInterval recurringInterval;

    private LocalDateTime nextRecurringDate;

    private LocalDateTime lastProcessed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status=TransactionStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id",nullable = false)
    private Account account;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;



}
