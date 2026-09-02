package com.devforge.repository;

import com.devforge.entity.Project;
import com.devforge.entity.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            SELECT p AS project, pm.projectRole AS role
            FROM Project p
            JOIN ProjectMember pm ON pm.id.projectId = p.id
            WHERE pm.id.userId = :userId
              AND p.deletedAt IS NULL
            ORDER BY p.updatedAt DESC
            """)
    List<ProjectWithRole> findAllAccessibleByUser(@Param("userId") Long userId);

    @Query("""
            SELECT p AS project, pm.projectRole AS role
            FROM Project p
            JOIN ProjectMember pm ON pm.id.projectId = p.id
            WHERE p.id = :projectId
              AND pm.id.userId = :userId
              AND p.deletedAt IS NULL
            """)
    Optional<ProjectWithRole> findAccessibleByIdWithRole(@Param("projectId") Long projectId,
                                                         @Param("userId") Long userId);

    Optional<Project> findByIdAndDeletedAtIsNull(Long projectId);

    interface ProjectWithRole {
        Project getProject();

        ProjectRole getRole();
    }
}
