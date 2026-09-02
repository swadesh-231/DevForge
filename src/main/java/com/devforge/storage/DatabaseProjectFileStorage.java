package com.devforge.storage;

import com.devforge.entity.ProjectFileContent;
import com.devforge.repository.ProjectFileContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseProjectFileStorage implements ProjectFileStorage {

    private static final String KEY_PREFIX = "projects/";

    private final ProjectFileContentRepository contentRepository;

    @Override
    public String storageKey(Long projectId, String path) {
        return KEY_PREFIX + projectId + "/" + path;
    }

    @Override
    @Transactional
    public void write(String storageKey, String content) {
        ProjectFileContent stored = contentRepository.findById(storageKey)
                .orElseGet(() -> ProjectFileContent.builder().storageKey(storageKey).build());
        stored.setContent(content);
        contentRepository.save(stored);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> read(String storageKey) {
        return contentRepository.findById(storageKey).map(ProjectFileContent::getContent);
    }

    @Override
    @Transactional
    public void delete(String storageKey) {
        contentRepository.deleteById(storageKey);
    }
}
