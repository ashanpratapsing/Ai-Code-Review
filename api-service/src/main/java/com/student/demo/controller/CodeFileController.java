package com.student.demo.controller;

import com.student.demo.dto.CodeFileDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.security.SecurityUtil;
import com.student.demo.service.CodeFileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code")
public class CodeFileController {

    private final CodeFileService codeFileService;
    private final SecurityUtil securityUtil;

    public CodeFileController(CodeFileService codeFileService, SecurityUtil securityUtil) {
        this.codeFileService = codeFileService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/upload")
    public CodeFile uploadCode(@RequestBody CodeFile codeFile) {
        if (codeFile.getContent() == null || codeFile.getContent().length() < 10) {
            throw new IllegalArgumentException("Code content is too short.");
        }
        if (codeFile.getContent().length() > 50000) {
            throw new IllegalArgumentException("Code content exceeds maximum allowed size (50k chars).");
        }
        return codeFileService.saveFile(codeFile, securityUtil.requireCurrentUserId());
    }

    @GetMapping("/files")
    public List<CodeFileDTO> getAllFiles(@RequestParam(required = false) Long projectId) {
        return codeFileService.getFilesForUser(securityUtil.requireCurrentUserId(), projectId);
    }

    @GetMapping("/files/{id}")
    public CodeFile getFile(@PathVariable Long id) {
        return codeFileService.getFileForUser(id, securityUtil.requireCurrentUserId());
    }

    @GetMapping("/project/{projectId}")
    public List<CodeFileDTO> getFilesByProject(@PathVariable Long projectId) {
        return codeFileService.getFilesForUser(securityUtil.requireCurrentUserId(), projectId);
    }
}
