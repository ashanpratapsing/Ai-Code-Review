package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "JAVA";
    }

    @Override
    public String getFilename(String code) {
        String className = "Main";
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            className = matcher.group(1);
        }
        return className + ".java";
    }

    @Override
    public String getDockerImage() {
        return "eclipse-temurin:17-alpine";
    }

    @Override
    public boolean hasCompilationStep() {
        return true;
    }

    @Override
    public String getCompileCommand(String filename) {
        return "javac " + filename;
    }

    @Override
    public String getRunCommand(String filename) {
        String className = filename.substring(0, filename.length() - 5);
        return "java " + className + " < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "Java Best Practices:\n" +
               "- Study Spring Boot patterns (e.g. dependency injection, transaction contexts).\n" +
               "- Focus on JVM memory usage, GC optimization, and memory leaks (e.g., open streams/connections).\n" +
               "- TreeMap/TreeSet usage requires explaining Red-Black tree balanced structures, sorting mechanics, and duplicate overrides.\n" +
               "- Multithreading requires explaining locks, race conditions, and thread safety.";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        // Core Java metadata parsed by standard analyzer.
    }
}
