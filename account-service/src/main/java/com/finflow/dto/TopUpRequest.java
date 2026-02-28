package com.finflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopUpRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum top up is Rp 10.000")
    private BigDecimal amount;
}
