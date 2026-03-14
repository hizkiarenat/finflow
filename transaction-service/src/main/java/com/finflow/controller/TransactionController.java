package com.finflow.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import com.finflow.dto.PageResponse;
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

    /**
     * GET /api/v1/transactions/history/{accountId}/paged
     *     ?page=0&size=10&sort=createdAt,desc
     *
     * Riwayat transaksi dengan pagination & sorting
     *
     * Default: halaman 0, 10 data per halaman, urut createdAt descending
     */
    @GetMapping("/history/{accountId}/paged")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getHistoryPaged(
            @PathVariable UUID accountId,
            @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<TransactionResponse> result =
                transactionService.getHistoryPaged(accountId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction history", result));
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
