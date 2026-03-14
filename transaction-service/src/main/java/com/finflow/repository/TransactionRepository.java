package com.finflow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finflow.dto.TransactionSummary;
import com.finflow.model.Transaction;

import feign.Param;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        // cari semua transaksi berdasarkan rekening pengirim
        @Query(value = """
                        SELECT *
                        FROM transactions
                        WHERE from_account_id = :fromAccountId
                        ORDER BY created_at DESC
                        """, nativeQuery = true)
        List<Transaction> findByFromAccountIdOrderByCreatedAtDesc(UUID fromAccountId);

        // cari semua transaksi berdasarkan rekening penerima
        @Query(value = """
                        SELECT *
                        FROM transactions
                        WHERE to_account_id = :toAccountId
                        ORDER BY created_at DESC
                        """, nativeQuery = true)
        List<Transaction> findByToAccountIdOrderByCreatedAtDesc(UUID toAccountId);

        /**
         * ambil riwayat transaksi sebuah rekening (sebagai pengirim ATAU penerima)
         * menggunakan OR condition dan ORDER BY 
         */
        @Query(value = """
                        SELECT * FROM transactions
                        WHERE from_account_id = :accountId
                           OR to_account_id   = :accountId
                        ORDER BY created_at DESC
                        LIMIT :limit
                        """, nativeQuery = true)
        List<Transaction> findTransactionHistory(
                        @Param("accountId") UUID accountId,
                        @Param("limit") int limit);

        /**
         * Riwayat transaksi dengan pagination & sorting
         * Menggantikan findTransactionHistory yang pakai LIMIT manual
         */
        @Query(value = """
                        SELECT * FROM transactions 
                        WHERE from_account_id = :accountId
                           OR to_account_id   = :accountId
                        """, nativeQuery = true)
        Page<Transaction> findTransactionHistoryPaged(
                        @Param("accountId") UUID accountId,
                        Pageable pageable);

        /**
         * ringkasan total transaksi per tipe untuk sebuah rekening
         * menggunakan GROUP BY dan SUM
         */
        @Query(value = """
                        SELECT
                            type,
                            COUNT(*)     AS total_count,
                            SUM(amount)  AS total_amount
                        FROM transactions
                        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
                          AND status = 'SUCCESS'
                        GROUP BY type
                        ORDER BY total_amount DESC
                        """, nativeQuery = true)
        List<TransactionSummary> getSummaryByAccountId(@Param("accountId") UUID accountId);
}
