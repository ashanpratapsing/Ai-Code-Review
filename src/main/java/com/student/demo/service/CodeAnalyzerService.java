package com.student.demo.service;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CodeAnalyzerService {

    @Autowired
    private MetricsRepository metricsRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(CodeAnalyzerService.class);

    public Metrics analyzeCode(CodeFile codeFile) {

        logger.info("Starting code analysis for file id: {}", codeFile.getId());

        String code = codeFile.getCodeContent();

        Metrics metrics = new Metrics();

        // Lines of Code
        int lines = code.split("\n").length;
        metrics.setLinesOfCode(lines);
        logger.info("Lines of Code: {}", lines);

        // Loop Count
        int loops = countOccurrences(code, "for");
        loops += countOccurrences(code, "while");
        metrics.setNumberOfLoops(loops);
        logger.info("Number of Loops: {}", loops);

        // Functions
        int functions = countOccurrences(code, "void");
        metrics.setNumberOfFunctions(functions);
        logger.info("Number of Functions: {}", functions);

        // Nested loops
        int nestedLoops = countOccurrences(code, "for") > 1 ? 1 : 0;
        metrics.setNestedLoops(nestedLoops);
        logger.info("Nested Loops: {}", nestedLoops);

        // Complexity Score
        int complexity = loops + functions + nestedLoops;
        metrics.setComplexityScore(complexity);
        logger.info("Complexity Score: {}", complexity);

        metrics.setCodeFile(codeFile);

        logger.info("Saving metrics to database");

        return metricsRepository.save(metrics);
    }

    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }
}