package com.finflow.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

/**
 * DTO untuk menerima response dari Account Service via Feign Client
 * Strukturnya mengikuti AccountResponse di account-service
 */
@Data
public class AccountResponse {
    private UUID id;
    private UUID userId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String status;
}