package com.finflow.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVING atau GIRO
}
