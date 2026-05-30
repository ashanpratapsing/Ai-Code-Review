package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;

@Component
public class CStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "C";
    }

    @Override
    public String getFilename(String code) {
        return "solution.c";
    }

    @Override
    public String getDockerImage() {
        return "gcc:alpine";
    }

    @Override
    public boolean hasCompilationStep() {
        return true;
    }

    @Override
    public String getCompileCommand(String filename) {
        return "gcc -O3 solution.c -o solution";
    }

    @Override
    public String getRunCommand(String filename) {
        return "./solution < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "C Memory & Safety Best Practices:\n" +
               "- Deeply analyze memory management: balance of malloc/calloc/realloc with free. Flag any memory leaks.\n" +
               "- Check for dangling pointers, double-free issues, and uninitialized pointers.\n" +
               "- Check for unsafe string operations (e.g., strcpy, strcat, sprintf, gets). Enforce bounds-checked variants (strncpy, snprintf).\n" +
               "- Audit array boundaries, pointer arithmetic logic, potential integer overflows, and segmentation fault patterns.\n" +
               "- Ensure proper validation of user inputs and error checks on system calls.";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        if (code.contains("malloc") || code.contains("calloc") || code.contains("realloc") || code.contains("free")) {
            meta.collections.add("malloc/free Manual Memory Management");
        }
        if (code.contains("strcpy") || code.contains("strcat") || code.contains("gets") || code.contains("sprintf")) {
            meta.isSecuritySensitive = true;
            meta.algorithms.add("Unsafe String Functions (strcpy/strcat/gets)");
        }
        if (code.contains("pthread_create") || code.contains("pthread_mutex")) {
            meta.hasMultithreading = true;
        }
    }
}
