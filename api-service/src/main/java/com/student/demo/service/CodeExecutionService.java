package com.student.demo.service;

import com.student.demo.dto.CodeExecutionDTO.*;
import com.student.demo.entity.CodeExecution;
import com.student.demo.entity.ExecutionLog;
import com.student.demo.entity.ExecutionTestResult;
import com.student.demo.entity.User;
import com.student.demo.repository.CodeExecutionRepository;
import com.student.demo.service.OwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final int MEMORY_LIMIT_MB = 128;
    private static final int TIMEOUT_SECONDS = 5;

    private final CodeExecutionRepository codeExecutionRepository;
    private final OwnershipService ownershipService;

    public CodeExecutionService(CodeExecutionRepository codeExecutionRepository, OwnershipService ownershipService) {
        this.codeExecutionRepository = codeExecutionRepository;
        this.ownershipService = ownershipService;
    }

    @Transactional
    public ExecutionResponse executeForUser(ExecutionRequest request, User user) {
        ExecutionResponse response = executeCode(request);
        persistExecution(request, user, response);
        return response;
    }

    public ExecutionResponse executeCode(ExecutionRequest request) {
        String language = request.getLanguage().toUpperCase();
        String code = request.getCode();
        List<TestCase> testCases = request.getTestCases();

        logger.info("Executing code in language: {}, with {} test cases", language, testCases != null ? testCases.size() : 0);

        Path tempDir = null;
        try {
            // Create a unique temporary directory inside the system temp folder
            tempDir = Files.createTempDirectory("sandbox_exec_" + UUID.randomUUID().toString());
            logger.info("Created temporary workspace: {}", tempDir.toAbsolutePath());

            String filename = "";
            String dockerImage = "";
            String runCmd = "";

            if ("JAVA".equals(language)) {
                // Find public class name to name the java file properly
                String className = "Main";
                Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
                Matcher matcher = pattern.matcher(code);
                if (matcher.find()) {
                    className = matcher.group(1);
                }
                filename = className + ".java";
                dockerImage = "eclipse-temurin:17-alpine";
                // Compile class and then run it
                runCmd = "javac " + filename + " && java " + className + " < input.txt";
            } else if ("PYTHON".equals(language)) {
                filename = "solution.py";
                dockerImage = "python:3.10-alpine";
                runCmd = "python " + filename + " < input.txt";
            } else if ("JAVASCRIPT".equals(language) || "JS".equals(language)) {
                filename = "script.js";
                dockerImage = "node:18-alpine";
                runCmd = "node " + filename + " < input.txt";
            } else {
                throw new IllegalArgumentException("Unsupported language: " + language);
            }

            // Write user source code to file
            Path codeFilePath = tempDir.resolve(filename);
            Files.writeString(codeFilePath, code);

            ExecutionResponse response = new ExecutionResponse();
            response.setStatus("SUCCESS");
            List<TestCaseResult> results = new ArrayList<>();

            // Perform pre-compilation check for Java to isolate COMPILE_ERROR from RUNTIME_ERROR
            if ("JAVA".equals(language)) {
                String hostPath = formatHostPath(tempDir.toAbsolutePath().toString());
                List<String> compileArgs = List.of(
                        "docker", "run", "--rm",
                        "--network", "none",
                        "-v", hostPath + ":/app",
                        "-w", "/app",
                        dockerImage,
                        "javac", filename
                );

                logger.info("Pre-compiling Java program using: {}", String.join(" ", compileArgs));

                File compileStdout = new File(tempDir.toFile(), "compile_stdout.txt");
                File compileStderr = new File(tempDir.toFile(), "compile_stderr.txt");

                ProcessBuilder compilePb = new ProcessBuilder(compileArgs);
                compilePb.directory(tempDir.toFile());
                compilePb.redirectOutput(compileStdout);
                compilePb.redirectError(compileStderr);

                Process compileProcess = compilePb.start();
                boolean compileCompleted = compileProcess.waitFor(10, TimeUnit.SECONDS);

                if (!compileCompleted) {
                    compileProcess.destroyForcibly();
                    response.setStatus("COMPILE_ERROR");
                    response.setCompileError("Compilation timed out.");
                    return response;
                }

                String compileErrors = Files.readString(compileStderr.toPath()).trim();
                String className = filename.substring(0, filename.length() - 5);
                File classFile = new File(tempDir.toFile(), className + ".class");

                if (!classFile.exists()) {
                    response.setStatus("COMPILE_ERROR");
                    response.setCompileError(compileErrors.isEmpty() ? "Compilation failed without detailed error output." : compileErrors);
                    return response;
                }
                
                // Redefine runCmd to execute directly since it's already compiled
                runCmd = "java " + className + " < input.txt";
            }

            // Run each testcase inside isolated Docker sandbox
            if (testCases != null && !testCases.isEmpty()) {
                for (TestCase tc : testCases) {
                    TestCaseResult res = runTestCase(tempDir, runCmd, dockerImage, tc);
                    results.add(res);
                }
            }

            response.setResults(results);
            return response;

        } catch (Exception e) {
            logger.error("Sandbox execution manager failed", e);
            ExecutionResponse errRes = new ExecutionResponse();
            errRes.setStatus("ERROR");
            errRes.setCompileError("Execution engine exception: " + e.getMessage());
            return errRes;
        } finally {
            if (tempDir != null) {
                cleanupDir(tempDir.toFile());
            }
        }
    }

    private TestCaseResult runTestCase(Path tempDir, String runCmd, String dockerImage, TestCase tc) {
        TestCaseResult result = new TestCaseResult();
        result.setId(tc.getId());
        result.setExpectedOutput(tc.getExpectedOutput());

        File inputFile = new File(tempDir.toFile(), "input.txt");
        File stdoutFile = new File(tempDir.toFile(), "stdout.txt");
        File stderrFile = new File(tempDir.toFile(), "stderr.txt");

        try {
            // Clean up files from previous runs
            Files.deleteIfExists(inputFile.toPath());
            Files.deleteIfExists(stdoutFile.toPath());
            Files.deleteIfExists(stderrFile.toPath());

            // Write inputs to input.txt
            Files.writeString(inputFile.toPath(), tc.getInput() != null ? tc.getInput() : "");

            String hostPath = formatHostPath(tempDir.toAbsolutePath().toString());

            // Construct standard Docker isolation command
            List<String> cmdArgs = List.of(
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--memory", "128m",
                    "--cpus", "0.5",
                    "-v", hostPath + ":/app",
                    "-w", "/app",
                    dockerImage,
                    "sh", "-c", runCmd
            );

            logger.info("Executing sandbox command for Testcase ID: {}", tc.getId());

            ProcessBuilder pb = new ProcessBuilder(cmdArgs);
            pb.directory(tempDir.toFile());
            pb.redirectOutput(stdoutFile);
            pb.redirectError(stderrFile);

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            // Set hard limit execution threshold to 5 seconds
            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(duration);

            if (!completed) {
                process.destroyForcibly();
                result.setStatus("TIMEOUT");
                result.setActualOutput("");
                result.setError("Time Limit Exceeded (TLE) - Exceeded 5s execution budget");
                return result;
            }

            int exitCode = process.exitValue();
            String actualOutput = Files.readString(stdoutFile.toPath()).trim();
            String errorOutput = Files.readString(stderrFile.toPath()).trim();

            result.setActualOutput(actualOutput);

            if (exitCode != 0) {
                result.setStatus("RUNTIME_ERROR");
                result.setError(errorOutput.isEmpty() ? "Process exited with code: " + exitCode : errorOutput);
            } else {
                // Ignore trailing spaces and newlines during comparison
                String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
                if (actualOutput.equals(expected)) {
                    result.setStatus("PASSED");
                } else {
                    result.setStatus("FAILED");
                    result.setError("Wrong Answer (WA) - Output mismatch");
                }
            }

        } catch (Exception e) {
            logger.error("Failed to run testcase ID: {}", tc.getId(), e);
            result.setStatus("RUNTIME_ERROR");
            result.setError("Sandbox setup exception: " + e.getMessage());
        }

        return result;
    }

    private String formatHostPath(String path) {
        path = path.replace("\\", "/");
        if (path.length() > 1 && path.charAt(1) == ':') {
            char drive = Character.toLowerCase(path.charAt(0));
            path = "/" + drive + path.substring(2);
        }
        return path;
    }

    private void cleanupDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                cleanupDir(f);
            }
        }
        file.delete();
    }

    private void persistExecution(ExecutionRequest request, User user, ExecutionResponse response) {
        CodeExecution execution = new CodeExecution();
        execution.setUser(user);
        execution.setLanguage(request.getLanguage().toUpperCase());
        execution.setSourceCode(request.getCode());
        execution.setStatus(response.getStatus());
        execution.setCompileError(response.getCompileError());
        execution.setMemoryLimitMb(MEMORY_LIMIT_MB);
        execution.setTimeoutMs(TIMEOUT_SECONDS * 1000);

        if (request.getCodeFileId() != null) {
            execution.setCodeFile(ownershipService.requireOwnedCodeFile(request.getCodeFileId(), user.getId()));
        }

        if (response.getResults() != null) {
            int order = 0;
            for (TestCaseResult result : response.getResults()) {
                ExecutionTestResult entity = new ExecutionTestResult();
                entity.setExecution(execution);
                entity.setCaseOrder(order++);
                entity.setStdin(findInput(request, result.getId()));
                entity.setExpectedOutput(result.getExpectedOutput());
                entity.setActualOutput(result.getActualOutput());
                entity.setStderr(result.getError());
                entity.setStatus(result.getStatus());
                entity.setErrorMessage(result.getError());
                entity.setExecutionTimeMs(result.getExecutionTimeMs());
                execution.getTestResults().add(entity);
            }
        }

        ExecutionLog log = new ExecutionLog();
        log.setExecution(execution);
        log.setLogLevel("INFO");
        log.setMessage("Execution completed with status: " + response.getStatus());
        execution.getLogs().add(log);

        codeExecutionRepository.save(execution);
        response.setExecutionId(execution.getId());
    }

    private String findInput(ExecutionRequest request, int caseId) {
        if (request.getTestCases() == null) {
            return null;
        }
        return request.getTestCases().stream()
                .filter(tc -> tc.getId() == caseId)
                .map(TestCase::getInput)
                .findFirst()
                .orElse(null);
    }
}
