package com.finflow.service.Impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.client.AccountClient;
import com.finflow.client.NotificationClient;
import com.finflow.dto.AccountResponse;
import com.finflow.dto.ApiResponse;
import com.finflow.dto.DepositRequest;
import com.finflow.dto.NotificationRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.dto.TransferRequest;
import com.finflow.exception.TransactionException;
import com.finflow.model.Transaction;
import com.finflow.repository.TransactionRepository;
import com.finflow.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    // transfer antar rekening
    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        // ambil data rek pengirim dari AccountService
        AccountResponse fromAccount = getAccount(request.getFromAccountNumber());

        // ambil data rek penerima dari AccountService
        AccountResponse toAccount = getAccount(request.getToAccountNumber());

        // validasi rek tidak boleh sama
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new TransactionException("Cannot transfer to same Account!");
        }

        // validasi saldo
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransactionException("Insufficient balance. current: " + fromAccount.getBalance());
        }

        // update rekening pengirim (amount negatif)
        accountClient.updateBalance(fromAccount.getId(), request.getAmount().negate());

        // update rekening penerima (amount positif)
        accountClient.updateBalance(toAccount.getId(), request.getAmount());

        // simpan transaksi
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber("TRF"))
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer success, ref: {}", saved.getReferenceNumber());

        // kirim notification
        sendNotification(fromAccount.getUserId(), "Transfer Successfully",
                "Transaction Rp " + request.getAmount() + "to " + request.getToAccountNumber() + " success",
                "TRANSFER");

        return TransactionResponse.fromEntity(saved);
    }

    // deposit ke rekening
    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        // ambil data rek dari accountService
        AccountResponse account = getAccount(request.getAccountNumber());

        // kredit saldo rekening
        accountClient.updateBalance(account.getId(), request.getAmount());

        // simpan transaksi
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber("DEP"))
                .toAccountId(account.getId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "DEPOSIT")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit success, ref: {}", saved.getReferenceNumber());

        // kirim notif
        sendNotification(account.getUserId(), "Deposit Successfully", "Deposit Rp " + request.getAmount() + " success",
                "DEPOSIT");
        return TransactionResponse.fromEntity(saved);
    }

    // riwayat transaksi
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(UUID accountId, int limit) {
        return transactionRepository.findTransactionHistory(accountId, limit)
                .stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ringkasan transaksi per tipe
   @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSummary(UUID accountId) {
        return transactionRepository.getSummaryByAccountId(accountId)
                .stream()
                .map(row -> {
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("type", row.getType());
                    summary.put("totalCount", row.getTotalCount());
                    summary.put("totalAmount", row.getTotalAmount());
                    return summary;
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Helper: ambil rekening dari Account Service
    // -------------------------------------------------------
    private AccountResponse getAccount(String accountNumber) {
        ApiResponse<AccountResponse> response = accountClient.getAccountByNumber(accountNumber);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new TransactionException("Account not found: " + accountNumber);
        }
        if (!"ACTIVE".equals(response.getData().getStatus())) {
            throw new TransactionException("Account is not active: " + accountNumber);
        }
        return response.getData();
    }

    // -------------------------------------------------------
    // Helper: kirim notifikasi ke Notification Service
    // -------------------------------------------------------
    private void sendNotification(UUID userId, String title, String message, String type) {
        try {
            notificationClient.sendNotification(
                    NotificationRequest.builder()
                            .userId(userId)
                            .title(title)
                            .message(message)
                            .type(type)
                            .build());
        } catch (Exception e) {
            // Log saja, jangan gagalkan transaksi karena notif gagal
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Helper: generate nomor referensi unik
    // -------------------------------------------------------
    private String generateReferenceNumber(String prefix) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return prefix + timestamp;
    }

}
