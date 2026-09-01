package com.devforge.service.impl;

import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileTreeResponse;
import com.devforge.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

    @Override
    public FileTreeResponse getFileTree(Long projectId) {
        return null;
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        return null;
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {

    }
}
