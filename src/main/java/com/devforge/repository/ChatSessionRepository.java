package com.devforge.repository;

import com.devforge.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    @Query("""
            SELECT s FROM ChatSession s
            WHERE s.project.id = :projectId
              AND s.user.id = :userId
              AND s.deletedAt IS NULL
            ORDER BY s.updatedAt DESC
            """)
    List<ChatSession> findActiveByProjectAndUser(@Param("projectId") Long projectId,
                                                 @Param("userId") Long userId);

    @Query("""
            SELECT s FROM ChatSession s
            WHERE s.id = :sessionId
              AND s.project.id = :projectId
              AND s.user.id = :userId
              AND s.deletedAt IS NULL
            """)
    Optional<ChatSession> findActiveByIdAndProjectAndUser(@Param("sessionId") Long sessionId,
                                                          @Param("projectId") Long projectId,
                                                          @Param("userId") Long userId);
}
