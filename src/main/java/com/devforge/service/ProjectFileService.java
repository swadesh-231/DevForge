package com.devforge.service;

import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileTreeResponse;

public interface ProjectFileService {

    FileTreeResponse getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    FileContentResponse saveFile(Long projectId, String path, String content);

    void deleteFile(Long projectId, String path);
}
