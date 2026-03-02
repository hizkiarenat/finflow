package com.finflow.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.finflow.dto.AccountResponse;
import com.finflow.dto.ApiResponse;

/**
 * Feign Client untuk komunikasi ke Account Service
 * Digunakan untuk:
 * 1. Cek rekening ada dan ACTIVE
 * 2. Update saldo setelah transaksi
 */
@FeignClient(name = "account-service", url = "${account-service.url}")
public interface AccountClient {

    @GetMapping("/api/v1/accounts/number/{accountNumber}")
    ApiResponse<AccountResponse> getAccountByNumber(@PathVariable String accountNumber);

    @PutMapping("/api/v1/accounts/{id}/balance")
    ApiResponse<Void> updateBalance(@PathVariable UUID id,
                                    @RequestParam BigDecimal amount);
}