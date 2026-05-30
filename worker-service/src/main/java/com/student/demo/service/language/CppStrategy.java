package com.student.demo.service.language;

import com.student.demo.service.SemanticCodeAnalyzer.CodeMetadata;
import org.springframework.stereotype.Component;

@Component
public class CppStrategy implements LanguageStrategy {

    @Override
    public String getLanguage() {
        return "CPP";
    }

    @Override
    public String getFilename(String code) {
        return "solution.cpp";
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
        return "g++ -std=c++17 -O3 solution.cpp -o solution";
    }

    @Override
    public String getRunCommand(String filename) {
        return "./solution < input.txt";
    }

    @Override
    public String getLanguageSpecificPrompt() {
        return "C++ OOP & STL Best Practices:\n" +
               "- Enforce RAII (Resource Acquisition Is Initialization) for managing locks, file descriptors, and memory.\n" +
               "- Encourage smart pointers (std::unique_ptr, std::shared_ptr) instead of raw new/delete memory management.\n" +
               "- Check for proper copy/move constructor implementations (Rule of Five, move semantics, std::move).\n" +
               "- Analyze STL container usage (std::vector, std::map) and potential iterator invalidation bugs.\n" +
               "- Audit templates, templates overhead, virtual destructor checks for inheritance hierarchies, and namespace pollution.\n" +
               "- Identify concurrency bugs in std::thread, std::mutex, or atomic values.";
    }

    @Override
    public void analyzeMetadata(String code, CodeMetadata meta) {
        if (code.contains("std::unique_ptr") || code.contains("std::shared_ptr") || code.contains("std::make_")) {
            meta.collections.add("C++ Smart Pointers (std::unique_ptr/std::shared_ptr)");
        }
        if (code.contains("std::vector") || code.contains("std::map") || code.contains("std::unordered_map")) {
            meta.collections.add("C++ STL Containers");
        }
        if (code.contains("template<") || code.contains("template <")) {
            meta.algorithms.add("C++ Generic Templates");
        }
        if (code.contains("std::thread") || code.contains("std::mutex") || code.contains("std::lock_guard")) {
            meta.hasMultithreading = true;
        }
    }
}
