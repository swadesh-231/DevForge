package com.devforge.service.impl;

import com.devforge.ai.prompt.ProjectRuntimeManifest;
import com.devforge.config.ProjectTemplateProperties;
import com.devforge.exception.FileStorageException;
import com.devforge.repository.ProjectFileRepository;
import com.devforge.service.ProjectFileService;
import com.devforge.service.ProjectTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ClasspathProjectTemplateService implements ProjectTemplateService {

    private static final String TEMPLATE_ROOT = "project-templates/";
    private static final String PACKAGE_JSON = "package.json";

    private final ProjectFileService projectFileService;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectTemplateProperties templateProperties;
    private final ResourcePatternResolver resourcePatternResolver;

    public ClasspathProjectTemplateService(ProjectFileService projectFileService,
                                           ProjectFileRepository projectFileRepository,
                                           ProjectTemplateProperties templateProperties,
                                           ResourceLoader resourceLoader) {
        this.projectFileService = projectFileService;
        this.projectFileRepository = projectFileRepository;
        this.templateProperties = templateProperties;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }

    @Override
    @Transactional
    public void initializeProjectFromTemplate(Long projectId) {
        if (projectFileRepository.countByProjectId(projectId) > 0) {
            log.debug("Project {} already has files, skipping template initialization", projectId);
            return;
        }

        String templateDirectory = TEMPLATE_ROOT + templateProperties.name() + "/";
        for (Resource resource : resolveTemplateResources(templateDirectory)) {
            if (!resource.isReadable()) {
                continue;
            }
            String path = relativePath(resource, templateDirectory);
            if (path.isEmpty() || path.endsWith("/")) {
                continue;
            }
            projectFileService.saveFile(projectId, path, read(resource));
        }

        projectFileService.saveFile(projectId, PACKAGE_JSON, ProjectRuntimeManifest.PACKAGE_JSON);
        log.info("Initialized project {} from template {}", projectId, templateProperties.name());
    }

    private Resource[] resolveTemplateResources(String templateDirectory) {
        try {
            Resource[] resources = resourcePatternResolver.getResources(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + templateDirectory + "**");
            if (resources.length == 0) {
                throw new FileStorageException("Project template " + templateProperties.name() + " is empty");
            }
            return resources;
        } catch (IOException exception) {
            throw new FileStorageException("Could not read project template " + templateProperties.name());
        }
    }

    private String relativePath(Resource resource, String templateDirectory) {
        try {
            String location = resource.getURL().toString();
            int start = location.lastIndexOf(templateDirectory);
            if (start < 0) {
                throw new FileStorageException("Template resource outside the template root: " + location);
            }
            return location.substring(start + templateDirectory.length());
        } catch (IOException exception) {
            throw new FileStorageException("Could not resolve the path of a template resource");
        }
    }

    private String read(Resource resource) {
        try (var stream = resource.getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new FileStorageException("Could not read template resource " + resource.getFilename());
        }
    }
}
