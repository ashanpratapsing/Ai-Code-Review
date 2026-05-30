package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;

@Component
public class JavaScriptStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "JAVASCRIPT";
    }

    @Override
    public String getFilename(String code) {
        return "script.js";
    }

    @Override
    public String getDockerImage() {
        return "node:18-alpine";
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
        return "node script.js < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "JavaScript Best Practices:\n" +
               "- Check for closure issues, event-loop blocking execution blocks, and prototype pollution.\n" +
               "- Ensure proper async/await handling, Promise chain resolution, and catching rejects.\n" +
               "- Enforce modern ES6 syntax (const/let, arrow functions, destructuring) and strict equality (===).";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        if (code.contains("worker_threads") || code.contains("cluster")) {
            meta.hasMultithreading = true;
        }
        if (code.contains("fs.readFileSync") || code.contains("fs.promises")) {
            meta.fileHandling.add("fs module");
        }
    }
}
