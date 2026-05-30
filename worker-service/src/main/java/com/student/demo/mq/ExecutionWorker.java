package com.student.demo.mq;

import com.student.demo.config.RabbitMQConfig;
import com.student.demo.service.language.LanguageStrategy;
import com.student.demo.dto.CodeExecutionDTO.*;
import com.student.demo.entity.CodeExecution;
import com.student.demo.entity.ExecutionLog;
import com.student.demo.entity.ExecutionTestResult;
import com.student.demo.repository.CodeExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExecutionWorker {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionWorker.class);
    private static final int MEMORY_LIMIT_MB = 256;
    private static final int TIMEOUT_SECONDS = 20;

    private final CodeExecutionRepository codeExecutionRepository;
    private final String sandboxVolumeName;
    private final List<LanguageStrategy> languageStrategies;

    public ExecutionWorker(CodeExecutionRepository codeExecutionRepository, List<LanguageStrategy> languageStrategies) {
        this.codeExecutionRepository = codeExecutionRepository;
        this.languageStrategies = languageStrategies;
        String vol = System.getenv("SANDBOX_VOLUME_NAME");
        this.sandboxVolumeName = (vol == null || vol.isEmpty()) ? "sandbox_data" : vol;
        logger.info("Initialized ExecutionWorker with sandboxVolumeName: {} and strategies count: {}", this.sandboxVolumeName, languageStrategies.size());
    }

    @RabbitListener(queues = RabbitMQConfig.EXECUTION_QUEUE_NAME)
    @Transactional
    public ExecutionResponse receiveExecutionRequest(ExecutionRequest request, @org.springframework.messaging.handler.annotation.Header(name = "x-death", required = false) java.util.List<java.util.Map<String, Object>> xDeath) {
        logger.info("Received execution job for execution ID: {}, language: {}", request.getExecutionId(), request.getLanguage());
        
        int retryCount = 0;
        if (xDeath != null && !xDeath.isEmpty()) {
            Long count = (Long) xDeath.get(0).get("count");
            retryCount = count.intValue();
        }

        if (retryCount >= 3) {
            logger.error("Max retries (3) reached via DLX for execution ID: {}. Dropping poison pill.", request.getExecutionId());
            return null; // Drop permanently
        }

        ExecutionResponse response = executeCode(request);
        
        try {
            persistExecutionResults(request, response);
        } catch (Exception e) {
            logger.error("Failed to persist execution results for job ID: {}", request.getExecutionId(), e);
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException(e);
        }

        return response;
    }

    private ExecutionResponse executeCode(ExecutionRequest request) {
        String language = request.getLanguage().toUpperCase();
        String code = request.getCode();
        List<TestCase> testCases = request.getTestCases();
        Long execId = request.getExecutionId();

        Path tempDir = null;
        try {
            File sandboxBaseDir = new File("/sandbox");
            if (!sandboxBaseDir.exists()) {
                sandboxBaseDir.mkdirs();
            }
            
            File execDir = new File(sandboxBaseDir, "exec_" + execId);
            execDir.mkdirs();
            
            // Set directory to be fully read/write/executable by non-root user (nobody/1000)
            execDir.setWritable(true, false);
            execDir.setReadable(true, false);
            execDir.setExecutable(true, false);
            
            tempDir = execDir.toPath();
            logger.info("Created isolated workspace: {}", tempDir.toAbsolutePath());

            LanguageStrategy strategy = getStrategy(language);
            if (strategy == null) {
                throw new IllegalArgumentException("Unsupported language: " + language);
            }

            String filename = strategy.getFilename(code);
            String dockerImage = strategy.getDockerImage();
            String runCmd = strategy.getRunCommand(filename);

            Path codeFilePath = tempDir.resolve(filename);
            Files.writeString(codeFilePath, code);
            // Ensure the code file itself is readable by the sandbox user
            codeFilePath.toFile().setReadable(true, false);

            ExecutionResponse response = new ExecutionResponse();
            response.setExecutionId(execId);
            response.setStatus("SUCCESS");
            List<TestCaseResult> results = new ArrayList<>();

            if (strategy.hasCompilationStep()) {
                String compileCmd = strategy.getCompileCommand(filename);
                // Compile step using hardened docker container running sh -c compileCmd
                List<String> compileArgs = List.of(
                        "docker", "run", "--rm",
                        "--network", "none",
                        "--memory", "256m",
                        "--cpus", "1.0",
                        "--user", "1000:1000",
                        "--read-only",
                        "--tmpfs", "/tmp",
                        "--pids-limit", "32",
                        "--cap-drop", "all",
                        "--security-opt", "no-new-privileges",
                        "-v", this.sandboxVolumeName + ":/sandbox",
                        "-w", "/sandbox/exec_" + execId,
                        dockerImage,
                        "sh", "-c", compileCmd
                );

                logger.info("Compiling {} code for job ID: {}", language, execId);

                File compileStdout = new File(tempDir.toFile(), "compile_stdout.txt");
                File compileStderr = new File(tempDir.toFile(), "compile_stderr.txt");

                ProcessBuilder compilePb = new ProcessBuilder(compileArgs);
                compilePb.directory(tempDir.toFile());
                compilePb.redirectOutput(compileStdout);
                compilePb.redirectError(compileStderr);

                Process compileProcess = compilePb.start();
                boolean compileCompleted = compileProcess.waitFor(45, TimeUnit.SECONDS);

                if (!compileCompleted) {
                    compileProcess.destroyForcibly();
                    response.setStatus("COMPILE_ERROR");
                    response.setCompileError("Compilation timed out (limit: 45s).");
                    return response;
                }

                int compileExitCode = compileProcess.exitValue();
                String compileErrors = Files.readString(compileStderr.toPath()).trim();

                if (compileExitCode != 0) {
                    response.setStatus("COMPILE_ERROR");
                    response.setCompileError(compileErrors.isEmpty() ? "Compilation failed with exit code " + compileExitCode : compileErrors);
                    return response;
                }
            }

            if (testCases != null && !testCases.isEmpty()) {
                for (TestCase tc : testCases) {
                    TestCaseResult res = runTestCase(tempDir, execId, runCmd, dockerImage, tc);
                    results.add(res);
                }
            }

            response.setResults(results);
            return response;

        } catch (Exception e) {
            logger.error("Sandbox execution failed for job ID: {}", execId, e);
            ExecutionResponse errRes = new ExecutionResponse();
            errRes.setExecutionId(execId);
            errRes.setStatus("ERROR");
            errRes.setCompileError("Execution engine exception: " + e.getMessage());
            return errRes;
        } finally {
            if (tempDir != null && tempDir.toFile().exists()) {
                cleanupDir(tempDir.toFile());
            }
        }
    }

    private TestCaseResult runTestCase(Path tempDir, Long execId, String runCmd, String dockerImage, TestCase tc) {
        TestCaseResult result = new TestCaseResult();
        result.setId(tc.getId());
        result.setExpectedOutput(tc.getExpectedOutput());

        File inputFile = new File(tempDir.toFile(), "input.txt");
        File stdoutFile = new File(tempDir.toFile(), "stdout.txt");
        File stderrFile = new File(tempDir.toFile(), "stderr.txt");

        try {
            if (inputFile.exists()) Files.delete(inputFile.toPath());
            if (stdoutFile.exists()) Files.delete(stdoutFile.toPath());
            if (stderrFile.exists()) Files.delete(stderrFile.toPath());

            Files.writeString(inputFile.toPath(), tc.getInput() != null ? tc.getInput() : "");
            inputFile.setReadable(true, false);

            // Hardened runtime container execution limits
            List<String> cmdArgs = List.of(
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--memory", "256m",
                    "--cpus", "1.0",
                    "--user", "1000:1000",
                    "--read-only",
                    "--tmpfs", "/tmp",
                    "--pids-limit", "32",
                    "--cap-drop", "all",
                    "--security-opt", "no-new-privileges",
                    "-v", this.sandboxVolumeName + ":/sandbox",
                    "-w", "/sandbox/exec_" + execId,
                    dockerImage,
                    "sh", "-c", runCmd
            );

            logger.debug("Running testcase ID: {} inside sandbox.", tc.getId());

            ProcessBuilder pb = new ProcessBuilder(cmdArgs);
            pb.directory(tempDir.toFile());
            pb.redirectOutput(stdoutFile);
            pb.redirectError(stderrFile);

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(duration);

            if (!completed) {
                process.destroyForcibly();
                result.setStatus("TIMEOUT");
                result.setActualOutput("");
                result.setError("Time Limit Exceeded (TLE) - Exceeded " + TIMEOUT_SECONDS + "s execution budget");
                return result;
            }

            int exitCode = process.exitValue();
            String actualOutput = stdoutFile.exists() ? Files.readString(stdoutFile.toPath()).trim() : "";
            String errorOutput = stderrFile.exists() ? Files.readString(stderrFile.toPath()).trim() : "";

            result.setActualOutput(actualOutput);

            if (exitCode != 0) {
                result.setStatus("RUNTIME_ERROR");
                result.setError(errorOutput.isEmpty() ? "Process exited with code: " + exitCode : errorOutput);
            } else {
                String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
                if (expected.isEmpty() || actualOutput.equals(expected)) {
                    result.setStatus("PASSED");
                } else {
                    result.setStatus("FAILED");
                    result.setError("Wrong Answer (WA) - Output mismatch");
                }
            }

        } catch (Exception e) {
            logger.error("Failed to run testcase ID: {} for execution ID: {}", tc.getId(), execId, e);
            result.setStatus("RUNTIME_ERROR");
            result.setError("Sandbox run exception: " + e.getMessage());
        }

        return result;
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

    private void persistExecutionResults(ExecutionRequest request, ExecutionResponse response) {
        Long executionId = request.getExecutionId();
        CodeExecution execution = codeExecutionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            logger.error("Could not find execution record with ID: {} to save results", executionId);
            return;
        }

        execution.setStatus(response.getStatus());
        execution.setCompileError(response.getCompileError());

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
        log.setMessage("Worker execution complete. Status: " + response.getStatus());
        execution.getLogs().add(log);

        codeExecutionRepository.save(execution);
        logger.info("Successfully persisted execution results in database for job ID: {}", executionId);
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

    private LanguageStrategy getStrategy(String language) {
        if (language == null || languageStrategies == null) return null;
        String upper = language.toUpperCase().trim();
        if ("JS".equals(upper)) {
            upper = "JAVASCRIPT";
        }
        final String searchLang = upper;
        return languageStrategies.stream()
                .filter(s -> s.getLanguage().equalsIgnoreCase(searchLang))
                .findFirst()
                .orElse(null);
    }
}
