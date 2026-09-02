package com.devforge.ai.tools;

import com.devforge.exception.ApiException;
import com.devforge.service.ProjectFileService;
import com.devforge.validation.ProjectFilePaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTools {

    private final ProjectFileService projectFileService;
    private final Long projectId;

    @Tool(name = "read_files",
            description = "Read the content of files. Only input file paths present inside the FILE_TREE. "
                    + "DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(
            @ToolParam(description = "List of project-relative paths, for example ['src/App.tsx']")
            List<String> paths) {

        List<String> contents = new ArrayList<>();
        if (paths == null) {
            return contents;
        }

        for (String path : paths) {
            contents.add(readFile(path));
        }
        return contents;
    }

    private String readFile(String path) {
        try {
            String normalizedPath = ProjectFilePaths.normalize(path);
            String content = projectFileService.getFileContent(projectId, normalizedPath).content();
            return """
                    --- START OF FILE: %s ---
                    %s
                    --- END OF FILE ---""".formatted(normalizedPath, content);
        } catch (ApiException exception) {
            log.debug("Tool read failed for {} in project {}: {}", path, projectId, exception.getMessage());
            return "--- FILE NOT AVAILABLE: %s (%s) ---".formatted(path, exception.getMessage());
        }
    }
}
