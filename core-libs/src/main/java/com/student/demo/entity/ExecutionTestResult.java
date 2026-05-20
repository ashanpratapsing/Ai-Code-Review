package com.student.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "execution_test_results")
public class ExecutionTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    private CodeExecution execution;

    @Column(name = "case_order", nullable = false)
    private int caseOrder;

    @Column(columnDefinition = "TEXT")
    private String stdin;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    @Column(columnDefinition = "TEXT")
    private String stderr;

    @Column(length = 50)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CodeExecution getExecution() { return execution; }
    public void setExecution(CodeExecution execution) { this.execution = execution; }
    public int getCaseOrder() { return caseOrder; }
    public void setCaseOrder(int caseOrder) { this.caseOrder = caseOrder; }
    public String getStdin() { return stdin; }
    public void setStdin(String stdin) { this.stdin = stdin; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
