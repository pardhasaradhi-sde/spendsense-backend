package com.spendsense.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptScanResponse {
    private String merchantName;
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;

    private String category;
    private String description;
    private String receiptUrl;
    private Double confidence;

    private String paymentMethod;
    private String taxAmount;
    private List<ReceiptItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItem {
        private String name;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal total;
    }
}