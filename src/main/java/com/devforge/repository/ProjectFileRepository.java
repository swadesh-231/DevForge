package com.devforge.repository;

import com.devforge.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

    List<ProjectFile> findByProjectIdOrderByPathAsc(Long projectId);

    Optional<ProjectFile> findByProjectIdAndPath(Long projectId, String path);

    boolean existsByProjectIdAndPath(Long projectId, String path);

    int countByProjectId(Long projectId);

    @Query("SELECT f.storageKey FROM ProjectFile f WHERE f.project.id = :projectId")
    List<String> findStorageKeysByProjectId(@Param("projectId") Long projectId);
}
