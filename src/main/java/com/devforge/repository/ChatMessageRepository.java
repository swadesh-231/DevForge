package com.devforge.repository;

import com.devforge.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT DISTINCT m FROM ChatMessage m
            LEFT JOIN FETCH m.events
            WHERE m.chatSession.id = :sessionId
            ORDER BY m.sequenceOrder ASC
            """)
    List<ChatMessage> findBySessionIdWithEvents(@Param("sessionId") Long sessionId);

    @Query("SELECT MAX(m.sequenceOrder) FROM ChatMessage m WHERE m.chatSession.id = :sessionId")
    Optional<Integer> findMaxSequenceOrder(@Param("sessionId") Long sessionId);
}
