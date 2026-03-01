package com.finflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.finflow.model.Transaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {

    private UUID id;
    private String referenceNumber;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction trx) {
        return TransactionResponse.builder()
                .id(trx.getId())
                .referenceNumber(trx.getReferenceNumber())
                .fromAccountId(trx.getFromAccountId())
                .toAccountId(trx.getToAccountId())
                .amount(trx.getAmount())
                .type(trx.getType().name())
                .status(trx.getStatus().name())
                .description(trx.getDescription())
                .createdAt(trx.getCreatedAt())
                .build();
    }
}
