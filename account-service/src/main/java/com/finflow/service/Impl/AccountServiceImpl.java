package com.finflow.service.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.client.UserClient;
import com.finflow.dto.AccountResponse;
import com.finflow.dto.ApiResponse;
import com.finflow.dto.CreateAccountRequest;
import com.finflow.dto.TopUpRequest;
import com.finflow.dto.UserResponse;
import com.finflow.exception.AccountNotFoundException;
import com.finflow.exception.InsufficientBalanceException;
import com.finflow.model.Account;
import com.finflow.repository.AccountRepository;
import com.finflow.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserClient userClient;

    // -------------------------------------------------------
    // Buka rekening baru
    // -------------------------------------------------------
    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for userId: {}", request.getUserId());
        ApiResponse<UserResponse> userResponse = userClient.getUserById(request.getUserId());

        // validasi user ada di User service via Feign client
        if (userResponse == null && !userResponse.isSuccess()) {
            throw new IllegalArgumentException("User not found! " + request.getUserId());
        }

        // validasi tipe rekening
        Account.AccountType accountType;
        try {
            accountType = Account.AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Account type must be SAVING or GIRO!");
        }

        Account account = Account.builder()
                .userId(request.getUserId())
                .accountNumber(generateAccountNumber())
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .currency("IDR")
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())

                .build();
        Account saved = accountRepository.save(account);
        return AccountResponse.fromEntity(saved);
    }

    // -------------------------------------------------------
    // Lihat rekening by ID
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + id));
        return AccountResponse.fromEntity(account);
    }

    // -------------------------------------------------------
    // Lihat rekening by nomor rekening
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with number: " + accountNumber));
        return AccountResponse.fromEntity(account);
    }

    // -------------------------------------------------------
    // Lihat semua rekening milik user
    // Java Stream: filter rekening yang tidak CLOSED, lalu transform ke DTO
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountByUserId(UUID userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .filter(acc -> acc.getStatus() != Account.AccountStatus.CLOSED)
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Top up saldo
    // -------------------------------------------------------
    @Override
    @Transactional
    public AccountResponse topUp(UUID accountId, TopUpRequest request) {
        // cek rekening ada dan ACTIVE
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot top up non-active account");
        }

        // update saldo
        accountRepository.updateBalance(accountId, request.getAmount());

        // return data terbaru
        return AccountResponse.fromEntity(
                accountRepository.findById(accountId).orElseThrow());
    }

    // -------------------------------------------------------
    // Update saldo — dipanggil oleh Transaction Service
    // amount positif = kredit, amount negatif = debit
    // -------------------------------------------------------
    @Override
    @Transactional
    public void updateBalance(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId));

        // Jika debit, pastikan saldo cukup
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal newBalance = account.getBalance().add(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Current balance: " + account.getBalance());
            }
        }

        accountRepository.updateBalance(accountId, amount);
    }

    // -------------------------------------------------------
    // Helper: generate nomor rekening unik
    // -------------------------------------------------------
    private String generateAccountNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 9999));
        String number = "1" + date + random;

        // Pastikan unik, generate ulang jika sudah ada
        while (accountRepository.existsByAccountNumber(number)) {
            random = String.format("%04d", (int) (Math.random() * 9999));
            number = "1" + date + random;
        }
        return number;
    }
}
