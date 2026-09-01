package com.devforge.service;

import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileTreeResponse;

public interface ProjectFileService {
    FileTreeResponse getFileTree(Long projectId);
    FileContentResponse getFileContent(Long projectId, String path);
    void saveFile(Long projectId, String filePath, String fileContent);
}
