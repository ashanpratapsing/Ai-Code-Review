package com.student.demo.controller;

import com.student.demo.dto.CodeExecutionDTO.ExecutionRequest;
import com.student.demo.dto.CodeExecutionDTO.ExecutionResponse;
import com.student.demo.security.SecurityUtil;
import com.student.demo.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class CodeExecutionController {

    private final CodeExecutionService executionService;
    private final SecurityUtil securityUtil;

    public CodeExecutionController(CodeExecutionService executionService, SecurityUtil securityUtil) {
        this.executionService = executionService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<ExecutionResponse> execute(@RequestBody ExecutionRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Source code cannot be empty");
        }
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Programming language must be specified");
        }
        if (request.getTestCases() == null || request.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("At least one test case is required");
        }

        ExecutionResponse response = executionService.executeForUser(request, securityUtil.requireCurrentUser());
        return ResponseEntity.ok(response);
    }
}
