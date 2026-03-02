package com.finflow.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finflow.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    // Cari rekening berdasarkan nomor rekening
    @Query(value = """
            SELECT * FROM accounts
            WHERE account_number = :accountNumber
            """, nativeQuery = true)
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    // Cari semua rekening milik satu user
    @Query(value = """
            SELECT * FROM accounts
            WHERE user_id = :userId
            """, nativeQuery = true)
    List<Account> findByUserId(@Param("userId") UUID userId);

    // Cek apakah nomor rekening sudah ada
    @Query(value = """
            SELECT COUNT(*) > 0
            FROM accounts
            WHERE account_number = :accountNumber
            """, nativeQuery = true)
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Mengambil semua rekening ACTIVE milik user
     * beserta total saldo menggunakan SUM dan GROUP BY
     */
    @Query(value = """
            SELECT
                user_id       AS userId,
                COUNT(*)      AS totalAccounts,
                SUM(balance)  AS totalBalance,
                currency      AS currency
            FROM accounts
            WHERE user_id = :userId
              AND status = 'ACTIVE'
            GROUP BY user_id, currency
            """, nativeQuery = true)
    List<Object[]> getTotalBalanceByUserId(@Param("userId") UUID userId);

    // Update saldo rekening
    @Modifying
    @Query(value = """
            UPDATE accounts
            SET balance    = balance + :amount,
                updated_at = CURRENT_TIMESTAMP
            WHERE id       = :accountId
              AND status   = 'ACTIVE'
            """, nativeQuery = true)
    int updateBalance(@Param("accountId") UUID accountId,
            @Param("amount") BigDecimal amount);

}
