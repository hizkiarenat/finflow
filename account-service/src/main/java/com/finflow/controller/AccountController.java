package com.finflow.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.dto.AccountResponse;
import com.finflow.dto.ApiResponse;
import com.finflow.dto.CreateAccountRequest;
import com.finflow.dto.TopUpRequest;
import com.finflow.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created Successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Account Found!", accountService.getAccountById(id)));
    }

    /**
     * Lihat rekening by nomor rekening
     * (Endpoint ini juga dipanggil oleh Transaction Service via Feign)
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(
                ApiResponse.success("Account found", accountService.getAccountByNumber(accountNumber)));
    }

    /**
     * Lihat semua rekening milik user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUserId(
            @PathVariable UUID userId) {
        List<AccountResponse> accounts = accountService.getAccountByUserId(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + accounts.size() + " accounts", accounts));
    }

     /**
     * Top up saldo rekening
     */
    @PostMapping("/{id}/topup")
    public ResponseEntity<ApiResponse<AccountResponse>> topUp(
            @PathVariable UUID id,
            @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Top up successful", accountService.topUp(id, request)));
    }

    /**
     * Update saldo — dipakai oleh Transaction Service
     * Contoh body: { "amount": -50000 } untuk debit, { "amount": 50000 } untuk kredit
     */
    @PatchMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<Void>> updateBalance(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        accountService.updateBalance(id, amount);
        return ResponseEntity.ok(ApiResponse.success("Balance updated", null));
    }
}
