package com.finflow.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.dto.ApiResponse;
import com.finflow.dto.DepositRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.dto.TransferRequest;
import com.finflow.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // transfer antar rekening
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transfer successfully", response));
    }

    // deposit ke rekening
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Deposit successfully", response));
    }

    // riwayat transaksi rekening
    @GetMapping("/history/{accountId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(@PathVariable UUID accountId,
            @RequestParam(defaultValue = "10") int limit) {
        List<TransactionResponse> response = transactionService.getHistory(accountId, limit);
        return ResponseEntity.ok(ApiResponse.success("Transaction History", response));
    }

    // ringkasan transaksi per tipe
    @GetMapping("/summary/{accountId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSummary(
            @PathVariable UUID accountId) {
        List<Map<String, Object>> summary = transactionService.getSummary(accountId);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction summary", summary));
    }

}
