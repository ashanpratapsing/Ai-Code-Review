package com.student.demo.dto;

import com.student.demo.dto.CodeExecutionDTO.TestCaseResult;

import java.util.List;

public class AnalysisContextDTO {
    private String executionStatus;
    private String compileError;
    private List<TestCaseResult> executionResults;

    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public String getCompileError() { return compileError; }
    public void setCompileError(String compileError) { this.compileError = compileError; }
    public List<TestCaseResult> getExecutionResults() { return executionResults; }
    public void setExecutionResults(List<TestCaseResult> executionResults) { this.executionResults = executionResults; }
}
