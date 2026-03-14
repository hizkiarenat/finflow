package com.finflow.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.finflow.dto.DepositRequest;
import com.finflow.dto.PageResponse;
import com.finflow.dto.TransactionResponse;
import com.finflow.dto.TransferRequest;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request);

    TransactionResponse deposit(DepositRequest request);

    List<TransactionResponse> getHistory(UUID accountId, int limit);

    PageResponse<TransactionResponse> getHistoryPaged(UUID accountId, Pageable pageable);

    List<Map<String, Object>> getSummary(UUID accountId);
}
