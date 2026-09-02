package com.devforge.service.impl;

import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileTreeResponse;
import com.devforge.entity.Project;
import com.devforge.entity.ProjectFile;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.ProjectFileMapper;
import com.devforge.repository.ProjectFileRepository;
import com.devforge.repository.ProjectRepository;
import com.devforge.service.ProjectFileService;
import com.devforge.storage.ProjectFileStorage;
import com.devforge.validation.ProjectFilePaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFileServiceImpl implements ProjectFileService {

    private static final String DEFAULT_MIME_TYPE = "text/plain";
    private static final Map<String, String> MIME_TYPES_BY_EXTENSION = Map.of(
            "js", "text/javascript",
            "jsx", "text/javascript",
            "ts", "text/typescript",
            "tsx", "text/typescript",
            "mjs", "text/javascript",
            "json", "application/json",
            "css", "text/css",
            "html", "text/html",
            "svg", "image/svg+xml",
            "md", "text/markdown"
    );

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectFileMapper projectFileMapper;
    private final ProjectFileStorage projectFileStorage;

    @Override
    public FileTreeResponse getFileTree(Long projectId) {
        List<ProjectFile> files = projectFileRepository.findByProjectIdOrderByPathAsc(projectId);
        return new FileTreeResponse(projectId, projectFileMapper.toFileNodes(files));
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        String normalizedPath = ProjectFilePaths.normalize(path);
        ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                .orElseThrow(() -> new ResourceNotFoundException("File", normalizedPath));

        String content = projectFileStorage.read(file.getStorageKey()).orElse("");
        return projectFileMapper.toFileContentResponse(file, content);
    }

    @Override
    @Transactional
    public FileContentResponse saveFile(Long projectId, String path, String content) {
        String normalizedPath = ProjectFilePaths.normalize(path);
        String body = content == null ? "" : content;

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        String storageKey = projectFileStorage.storageKey(projectId, normalizedPath);
        projectFileStorage.write(storageKey, body);

        ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                .orElseGet(() -> ProjectFile.builder()
                        .project(project)
                        .path(normalizedPath)
                        .storageKey(storageKey)
                        .build());

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        file.setStorageKey(storageKey);
        file.setSizeBytes((long) bytes.length);
        file.setContentHash(sha256(bytes));
        file.setMimeType(mimeTypeOf(normalizedPath));

        ProjectFile saved = projectFileRepository.save(file);
        log.debug("Saved file {} for project {}", normalizedPath, projectId);
        return projectFileMapper.toFileContentResponse(saved, body);
    }

    @Override
    @Transactional
    public void deleteFile(Long projectId, String path) {
        String normalizedPath = ProjectFilePaths.normalize(path);
        ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                .orElseThrow(() -> new ResourceNotFoundException("File", normalizedPath));

        projectFileRepository.delete(file);
        projectFileStorage.delete(file.getStorageKey());
    }

    private static String mimeTypeOf(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < path.length() - 1) {
            String mimeType = MIME_TYPES_BY_EXTENSION.get(path.substring(lastDot + 1).toLowerCase());
            if (mimeType != null) {
                return mimeType;
            }
        }
        String guessed = URLConnection.guessContentTypeFromName(path);
        return guessed != null ? guessed : DEFAULT_MIME_TYPE;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
