package com.student.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.demo.ai.AIService;
import com.student.demo.entity.AIReport;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.AIReportRepository;
import com.student.demo.repository.CodeFileRepository;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private CodeFileRepository codeFileRepository;

    @Autowired
    private AIReportRepository aiReportRepository;

    @PostMapping("/review/{fileId}")
    public AIReport reviewCode(@PathVariable Long fileId) {
        CodeFile file = codeFileRepository.findById(fileId).orElseThrow();
        return aiService.generateReview(file);
    }

    @GetMapping("/{fileId}")
    public AIReport getReport(@PathVariable Long fileId) {
        return aiReportRepository.findByCodeFileId(fileId).orElseThrow();
    }
}