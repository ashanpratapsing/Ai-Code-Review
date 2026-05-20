package com.student.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_executions")
public class CodeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_file_id")
    private CodeFile codeFile;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "compile_error", columnDefinition = "TEXT")
    private String compileError;

    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb = 128;

    @Column(name = "timeout_ms")
    private Integer timeoutMs = 5000;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionTestResult> testResults = new ArrayList<>();

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionLog> logs = new ArrayList<>();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public CodeFile getCodeFile() { return codeFile; }
    public void setCodeFile(CodeFile codeFile) { this.codeFile = codeFile; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCompileError() { return compileError; }
    public void setCompileError(String compileError) { this.compileError = compileError; }
    public Integer getMemoryLimitMb() { return memoryLimitMb; }
    public void setMemoryLimitMb(Integer memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ExecutionTestResult> getTestResults() { return testResults; }
    public void setTestResults(List<ExecutionTestResult> testResults) { this.testResults = testResults; }
    public List<ExecutionLog> getLogs() { return logs; }
    public void setLogs(List<ExecutionLog> logs) { this.logs = logs; }
}
