package com.devforge.repository;

import com.devforge.entity.ProjectMember;
import com.devforge.entity.ProjectMemberId;
import com.devforge.entity.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    @Query("""
            SELECT pm FROM ProjectMember pm
            JOIN FETCH pm.user
            WHERE pm.id.projectId = :projectId
            ORDER BY pm.invitedAt ASC
            """)
    List<ProjectMember> findByProjectIdWithUser(@Param("projectId") Long projectId);

    Optional<ProjectMember> findByIdProjectIdAndIdUserId(Long projectId, Long userId);

    boolean existsByIdProjectIdAndIdUserId(Long projectId, Long userId);

    @Query("""
            SELECT pm.projectRole FROM ProjectMember pm
            WHERE pm.id.projectId = :projectId AND pm.id.userId = :userId
            """)
    Optional<ProjectRole> findRoleByProjectIdAndUserId(@Param("projectId") Long projectId,
                                                       @Param("userId") Long userId);

    @Query("""
            SELECT COUNT(pm) FROM ProjectMember pm
            JOIN pm.project p
            WHERE pm.id.userId = :userId
              AND pm.projectRole = com.devforge.entity.enums.ProjectRole.OWNER
              AND pm.acceptedAt IS NOT NULL
              AND p.deletedAt IS NULL
            """)
    int countProjectsOwnedByUser(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(pm) FROM ProjectMember pm
            WHERE pm.id.projectId = :projectId
              AND pm.projectRole = com.devforge.entity.enums.ProjectRole.OWNER
            """)
    int countOwners(@Param("projectId") Long projectId);

    @Query("""
            SELECT pm FROM ProjectMember pm
            JOIN FETCH pm.project p
            WHERE pm.id.userId = :userId
              AND pm.acceptedAt IS NULL
              AND p.deletedAt IS NULL
            ORDER BY pm.invitedAt DESC
            """)
    List<ProjectMember> findPendingInvitationsByUserId(@Param("userId") Long userId);
}
