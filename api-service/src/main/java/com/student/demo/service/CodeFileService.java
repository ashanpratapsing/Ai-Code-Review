package com.student.demo.service;

import com.student.demo.dto.CodeFileDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Project;
import com.student.demo.entity.User;
import com.student.demo.exception.ForbiddenException;
import com.student.demo.repository.CodeFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeFileService {

    private static final Logger logger = LoggerFactory.getLogger(CodeFileService.class);

    private final CodeFileRepository codeFileRepository;
    private final OwnershipService ownershipService;

    public CodeFileService(CodeFileRepository codeFileRepository, OwnershipService ownershipService) {
        this.codeFileRepository = codeFileRepository;
        this.ownershipService = ownershipService;
    }

    public CodeFile saveFile(CodeFile codeFile, Long userId) {
        if (codeFile.getContent() == null) {
            throw new IllegalArgumentException("Code content cannot be null");
        }
        if (codeFile.getProject() == null || codeFile.getProject().getId() == null) {
            throw new IllegalArgumentException("project.id is required");
        }

        Project project = ownershipService.requireOwnedProject(codeFile.getProject().getId(), userId);
        codeFile.setProject(project);
        logger.info("Saving code file '{}' for user {} in project {}", codeFile.getName(), userId, project.getId());
        return codeFileRepository.save(codeFile);
    }

    public List<CodeFileDTO> getFilesForUser(Long userId, Long projectId) {
        List<CodeFile> files = projectId != null
                ? codeFileRepository.findByProjectIdForUser(projectId, userId)
                : codeFileRepository.findAllByUserId(userId);

        return files.stream()
                .map(file -> new CodeFileDTO(file.getId(), file.getName(), file.getLanguage(), file.getContent(), file.getProject().getId()))
                .toList();
    }

    public CodeFile getFileForUser(Long fileId, Long userId) {
        return codeFileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new ForbiddenException("Code file not found or access denied"));
    }
}
