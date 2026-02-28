package com.finflow.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finflow.dto.MonthlySummary;
import com.finflow.dto.UserStatusCount;
import com.finflow.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = """
            SELECT * FROM users
            WHERE email = :email 
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findByEmail(@Param("email")String email);

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)
            """, nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM users WHERE phone_number = :phoneNumber)
            """, nativeQuery = true)
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    //  mencari user berdasarkan nama/email yang statusnya active
    @Query(value = """
            SELECT * FROM users
            WHERE status = 'ACTIVE'
            AND (
                 full_name ILIKE '%' || :keyword || '%'
                 OR email ILIKE '%' || :keyword || '%'
            )
             ORDER BY created_at DESC
            """, nativeQuery = true)
    List<User> searchActiveUser(@Param("keyword") String keyword);

    // ambil jumlah user berdasarkan status
    @Query(value = """
            SELECT status, COUNT(*) as total
            FROM users
            GROUP BY status
            ORDER BY total DESC
            """, nativeQuery = true)
    List<UserStatusCount> countUsersByStatus();

    // cari user dalam rentang waktu tertentu dan user yang statusny blm pernah dinonaktifkan
    @Query(value = """
            SELECT * FROM users u
            WHERE u.created_at >= CURRENT_DATE - INTERVAL ':days days'
            AND u.id NOT IN (
                SELECT id FROM users WHERE status ='INACTIVE'
            )
            ORDER BY u.created_at DESC
            """, nativeQuery = true)
    List<User> findNewActiveUsers(@Param("days") int days);

    // mengambil user terbaru per bulan
    @Query(value = """
            SELECT
                TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS month,
                COUNT(*) AS total_registered,
                COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) AS total_active
            FROM users
            GROUP BY DATE_TRUNC('month', created_at)
            ORDER BY month DESC
            LIMIT 12
            """, nativeQuery = true)
    List<MonthlySummary> getMonthlyRegistrationSummary();

}
