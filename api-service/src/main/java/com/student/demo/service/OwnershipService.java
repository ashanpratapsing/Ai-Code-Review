package com.student.demo.service;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.entity.Project;
import com.student.demo.exception.ForbiddenException;
import com.student.demo.exception.ResourceNotFoundException;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.repository.MetricsRepository;
import com.student.demo.repository.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private final ProjectRepository projectRepository;
    private final CodeFileRepository codeFileRepository;
    private final MetricsRepository metricsRepository;

    public OwnershipService(ProjectRepository projectRepository,
                            CodeFileRepository codeFileRepository,
                            MetricsRepository metricsRepository) {
        this.projectRepository = projectRepository;
        this.codeFileRepository = codeFileRepository;
        this.metricsRepository = metricsRepository;
    }

    public Project requireOwnedProject(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Project not found or access denied"));
    }

    public CodeFile requireOwnedCodeFile(Long fileId, Long userId) {
        return codeFileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new ForbiddenException("Code file not found or access denied"));
    }

    public Metrics requireOwnedMetrics(Long fileId, Long userId) {
        return metricsRepository.findByCodeFileIdAndUserId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis metrics not found"));
    }

    public void assertProjectOwned(Long projectId, Long userId) {
        requireOwnedProject(projectId, userId);
    }

    public void assertCodeFileOwned(Long fileId, Long userId) {
        requireOwnedCodeFile(fileId, userId);
    }
}
