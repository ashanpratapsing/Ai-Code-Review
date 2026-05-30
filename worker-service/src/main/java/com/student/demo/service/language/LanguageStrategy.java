package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;

public interface LanguageStrategy {
    String getLanguage();
    String getFilename(String code);
    String getDockerImage();
    boolean hasCompilationStep();
    String getCompileCommand(String filename);
    String getRunCommand(String filename);
    String getLanguageSpecificPrompt();
    void analyzeMetadata(String code, CodeMetadata meta);
}
