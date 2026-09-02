package com.devforge.repository;

import com.devforge.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    @Query("""
            SELECT u FROM UsageRecord u
            WHERE u.user.id = :userId AND u.date = :date
            """)
    Optional<UsageRecord> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO usage_records (user_id, date, tokens_in, tokens_out, message_count, preview_seconds)
            VALUES (:userId, :date, :tokensIn, :tokensOut, :messageCount, 0)
            ON CONFLICT (user_id, date) DO UPDATE SET
                tokens_in = usage_records.tokens_in + EXCLUDED.tokens_in,
                tokens_out = usage_records.tokens_out + EXCLUDED.tokens_out,
                message_count = usage_records.message_count + EXCLUDED.message_count
            """, nativeQuery = true)
    void addUsage(@Param("userId") Long userId,
                  @Param("date") LocalDate date,
                  @Param("tokensIn") int tokensIn,
                  @Param("tokensOut") int tokensOut,
                  @Param("messageCount") int messageCount);
}
