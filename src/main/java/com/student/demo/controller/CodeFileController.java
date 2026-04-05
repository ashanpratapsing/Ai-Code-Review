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
        return codeFileService.saveFile(codeFile);
    }

    @GetMapping("/files")
    public List<CodeFileDTO> getAllFiles() {
        List<CodeFile> files = codeFileRepository.findAll();

        return files.stream()
                .map(file -> new CodeFileDTO(
                        file.getId(),
                        file.getFileName(),
                        file.getLanguage()
                ))
                .toList();
    }

    @GetMapping("/project/{projectId}")
    public List<CodeFile> getFilesByProject(@PathVariable Long projectId) {
        return codeFileRepository.findByProjectId(projectId);
    }
}