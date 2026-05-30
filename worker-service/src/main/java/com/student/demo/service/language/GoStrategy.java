package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;

@Component
public class GoStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "GO";
    }

    @Override
    public String getFilename(String code) {
        return "main.go";
    }

    @Override
    public String getDockerImage() {
        return "golang:alpine";
    }

    @Override
    public boolean hasCompilationStep() {
        return true;
    }

    @Override
    public String getCompileCommand(String filename) {
        return "HOME=/tmp GOCACHE=/tmp/go-cache GOPATH=/tmp/go-path go build -o solution main.go";
    }

    @Override
    public String getRunCommand(String filename) {
        return "./solution < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "Go Best Practices:\n" +
               "- Audit goroutine lifetimes: check for potential goroutine leaks (e.g., blocking sends on unbuffered channels).\n" +
               "- Check for channel deadlocks, proper close checks, and use of select with default/timeout handles.\n" +
               "- Validate Context propagation (ensuring ctx context.Context is passed properly to blocking / I/O functions).\n" +
               "- Enforce idiomatic Go error handling (check every err return, wrap errors appropriately, avoid unnecessary panic).\n" +
               "- Audit interface segregation principles, struct embedding, and atomic vs mutex race condition risks.";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        if (code.contains("go ") || code.contains("chan ") || code.contains("select {")) {
            meta.hasMultithreading = true;
            meta.collections.add("Go Channels/Goroutines Concurrency");
        }
        if (code.contains("context.Context") || code.contains("context.With")) {
            meta.collections.add("Go Context Propagation");
        }
        if (code.contains("defer ")) {
            meta.algorithms.add("Go Defer Resource Release");
        }
    }
}
