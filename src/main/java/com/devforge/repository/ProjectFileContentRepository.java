package com.devforge.repository;

import com.devforge.entity.ProjectFileContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectFileContentRepository extends JpaRepository<ProjectFileContent, String> {

    void deleteAllByStorageKeyIn(Iterable<String> storageKeys);
}
