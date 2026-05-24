package com.student.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalysisResponse {
    private String summary;
    private String codeQuality;
    private String timeComplexity;
    private String spaceComplexity;
    private String explanation;
    private List<String> bugsDetected;
    private List<String> securityIssues;
    private List<String> suggestions;
    private String betterApproach;
    private String optimizedCode;
    private String designPattern;
    private String faangInsights;
    private List<String> edgeCases;
    private List<String> performanceIssues;
    private List<String> bestPractices;
    private List<String> codeSmells;
    private String scalabilityAnalysis;
    private String readabilityScore;
    private String maintainabilityScore;
    private String concurrencyAnalysis;
    private String collectionAnalysis;
    private String graphAnalysis;
    private String runtimeAnalysis;
}
