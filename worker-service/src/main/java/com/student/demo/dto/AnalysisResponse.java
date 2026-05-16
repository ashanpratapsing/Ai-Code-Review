package com.student.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalysisResponse {
    private String summary;
    private List<String> issues;
    private String betterApproach;
    private String timeComplexity;
    private String spaceComplexity;
    private String optimizedCode;
    private String faangInsights;
    private List<String> securityVulnerabilities;
    private String architecturalDebt;
    private Integer maintainabilityScore;
}
