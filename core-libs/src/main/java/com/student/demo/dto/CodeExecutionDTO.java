package com.student.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class CodeExecutionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionRequest implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Long executionId;
        private String code;
        private String language;
        private List<TestCase> testCases;
        private Long codeFileId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String input;
        private String expectedOutput;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionResponse implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Long executionId;
        private String status; // SUCCESS, COMPILE_ERROR, ERROR
        private String compileError;
        private List<TestCaseResult> results;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String status; // PASSED, FAILED, RUNTIME_ERROR, TIMEOUT
        private String actualOutput;
        private String expectedOutput;
        private String error;
        private long executionTimeMs;
    }
}
