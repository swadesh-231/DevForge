package com.devforge.repository;

import com.devforge.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
