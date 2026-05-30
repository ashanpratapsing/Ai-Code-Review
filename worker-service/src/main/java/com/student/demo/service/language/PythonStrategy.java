package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;

@Component
public class PythonStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "PYTHON";
    }

    @Override
    public String getFilename(String code) {
        return "solution.py";
    }

    @Override
    public String getDockerImage() {
        return "python:3.10-alpine";
    }

    @Override
    public boolean hasCompilationStep() {
        return false;
    }

    @Override
    public String getCompileCommand(String filename) {
        return null;
    }

    @Override
    public String getRunCommand(String filename) {
        return "python solution.py < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "Python Best Practices:\n" +
               "- Strict PEP 8 styling conventions (naming, spacing, comments).\n" +
               "- Efficient use of decorators, generators, and list comprehensions.\n" +
               "- Prevent global variables and analyze GIL constraints or async loop efficiency.";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        int count = countOccurrences(code, "for\\s+\\w+\\s+in") + countOccurrences(code, "while\\s+");
        if (count > 0) {
            meta.loopsCount = count;
        }
        if (code.contains("import threading") || code.contains("import asyncio") || code.contains("multiprocessing")) {
            meta.hasMultithreading = true;
        }
    }

    private int countOccurrences(String text, String regex) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
