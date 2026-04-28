package com.student.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResponse {
    private String summary;
    private List<String> issues;
    private String betterApproach;
    private String timeComplexity;
    private String spaceComplexity;
    private String optimizedCode;
    private String faangInsights;

    public AnalysisResponse() {}

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getIssues() { return issues; }
    public void setIssues(List<String> issues) { this.issues = issues; }
    public String getBetterApproach() { return betterApproach; }
    public void setBetterApproach(String betterApproach) { this.betterApproach = betterApproach; }
    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }
    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }
    public String getOptimizedCode() { return optimizedCode; }
    public void setOptimizedCode(String optimizedCode) { this.optimizedCode = optimizedCode; }
    public String getFaangInsights() { return faangInsights; }
    public void setFaangInsights(String faangInsights) { this.faangInsights = faangInsights; }
}
