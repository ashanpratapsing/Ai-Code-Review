package com.student.demo.controller;

import com.student.demo.dto.CodeFileDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.service.CodeFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code")
public class CodeFileController {

    @Autowired
    private CodeFileService codeFileService;

    @Autowired
    private CodeFileRepository codeFileRepository;

    @PostMapping("/upload")
    public CodeFile uploadCode(@RequestBody CodeFile codeFile) {
        if (codeFile.getContent() == null || codeFile.getContent().length() < 10) {
            throw new IllegalArgumentException("Code content is too short.");
        }
        if (codeFile.getContent().length() > 50000) {
            throw new IllegalArgumentException("Code content exceeds maximum allowed size (50k chars).");
        }
        return codeFileService.saveFile(codeFile);
    }

    @GetMapping("/files")
    public List<CodeFileDTO> getAllFiles(@RequestParam(required = false) Long projectId) {
        List<CodeFile> files;
        if (projectId != null) {
            files = codeFileRepository.findByProjectId(projectId);
        } else {
            files = codeFileRepository.findAll();
        }

        return files.stream()
                .map(file -> new CodeFileDTO(
                        file.getId(),
                        file.getName(),
                        file.getLanguage(),
                        file.getContent()
                ))
                .toList();
    }

    @GetMapping("/files/{id}")
    public CodeFile getFile(@PathVariable Long id) {
        return codeFileRepository.findById(id).orElseThrow();
    }

    @GetMapping("/project/{projectId}")
    public List<CodeFile> getFilesByProject(@PathVariable Long projectId) {
        return codeFileRepository.findByProjectId(projectId);
    }
}
