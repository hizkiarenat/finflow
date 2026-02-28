package com.finflow.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.finflow.dto.AccountResponse;
import com.finflow.dto.CreateAccountRequest;
import com.finflow.dto.TopUpRequest;

public interface AccountService {

    // Buka rekening baru
    AccountResponse createAccount(CreateAccountRequest request);

    // Lihat rekening by ID
    AccountResponse getAccountById(UUID id);

    // Lihat rekening by nomor rekening
    AccountResponse getAccountByNumber(String accountNumber);

    // Lihat semua rekening milik user
    List<AccountResponse> getAccountByUserId(UUID userId);

    // Top up saldo
    AccountResponse topUp(UUID accountId, TopUpRequest request);

    // Update saldo (dipakai Transaction Service)
    void updateBalance(UUID accountId, BigDecimal amount);
}
