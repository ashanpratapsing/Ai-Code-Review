package com.student.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "metrics")
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    private int linesOfCode;
    private int numberOfFunctions;
    private int numberOfLoops;
    private int nestedLoops;
    private int complexityScore;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String codeQuality;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String bugs;

    @Column(columnDefinition = "TEXT")
    private String optimization;

    @Column(columnDefinition = "TEXT")
    private String codeSmells;

    @Column(length = 50)
    private String timeComplexity;

    @Column(columnDefinition = "TEXT")
    private String refactoredCode;

    @Column(columnDefinition = "TEXT")
    private String unitTests;

    @Column(columnDefinition = "TEXT")
    private String betterApproach;

    @Column(length = 50)
    private String spaceComplexity;

    @Column(columnDefinition = "TEXT")
    private String faangInsights;

    @Column(columnDefinition = "TEXT")
    private String securityIssues;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(columnDefinition = "TEXT")
    private String designPattern;

    @Column(columnDefinition = "TEXT")
    private String edgeCases;

    @Column(columnDefinition = "TEXT")
    private String performanceIssues;

    @Column(columnDefinition = "TEXT")
    private String bestPractices;

    @Column(columnDefinition = "TEXT")
    private String scalabilityAnalysis;

    @Column(columnDefinition = "TEXT")
    private String concurrencyAnalysis;

    @Column(columnDefinition = "TEXT")
    private String collectionAnalysis;

    @Column(columnDefinition = "TEXT")
    private String graphAnalysis;

    @Column(columnDefinition = "TEXT")
    private String runtimeAnalysis;

    private int readabilityScore;
    private int maintainabilityScore;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private int retryCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_file_id")
    private CodeFile codeFile;

    public Metrics() {
        this.status = AnalysisStatus.PENDING;
    }

    public Long getId() { return id; }
    public int getLinesOfCode() { return linesOfCode; }
    public void setLinesOfCode(int linesOfCode) { this.linesOfCode = linesOfCode; }
    public int getNumberOfFunctions() { return numberOfFunctions; }
    public void setNumberOfFunctions(int numberOfFunctions) { this.numberOfFunctions = numberOfFunctions; }
    public int getNumberOfLoops() { return numberOfLoops; }
    public void setNumberOfLoops(int numberOfLoops) { this.numberOfLoops = numberOfLoops; }
    public int getNestedLoops() { return nestedLoops; }
    public void setNestedLoops(int nestedLoops) { this.nestedLoops = nestedLoops; }
    public int getComplexityScore() { return complexityScore; }
    public void setComplexityScore(int complexityScore) { this.complexityScore = complexityScore; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCodeQuality() { return codeQuality; }
    public void setCodeQuality(String codeQuality) { this.codeQuality = codeQuality; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getBugs() { return bugs; }
    public void setBugs(String bugs) { this.bugs = bugs; }
    public String getOptimization() { return optimization; }
    public void setOptimization(String optimization) { this.optimization = optimization; }
    public String getCodeSmells() { return codeSmells; }
    public void setCodeSmells(String codeSmells) { this.codeSmells = codeSmells; }
    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }
    public String getRefactoredCode() { return refactoredCode; }
    public void setRefactoredCode(String refactoredCode) { this.refactoredCode = refactoredCode; }
    public String getUnitTests() { return unitTests; }
    public void setUnitTests(String unitTests) { this.unitTests = unitTests; }
    public String getBetterApproach() { return betterApproach; }
    public void setBetterApproach(String betterApproach) { this.betterApproach = betterApproach; }
    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }
    public String getFaangInsights() { return faangInsights; }
    public void setFaangInsights(String faangInsights) { this.faangInsights = faangInsights; }

    public String getSecurityIssues() { return securityIssues; }
    public void setSecurityIssues(String securityIssues) { this.securityIssues = securityIssues; }
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    public String getDesignPattern() { return designPattern; }
    public void setDesignPattern(String designPattern) { this.designPattern = designPattern; }
    public String getEdgeCases() { return edgeCases; }
    public void setEdgeCases(String edgeCases) { this.edgeCases = edgeCases; }
    public String getPerformanceIssues() { return performanceIssues; }
    public void setPerformanceIssues(String performanceIssues) { this.performanceIssues = performanceIssues; }
    public String getBestPractices() { return bestPractices; }
    public void setBestPractices(String bestPractices) { this.bestPractices = bestPractices; }
    public String getScalabilityAnalysis() { return scalabilityAnalysis; }
    public void setScalabilityAnalysis(String scalabilityAnalysis) { this.scalabilityAnalysis = scalabilityAnalysis; }

    public String getConcurrencyAnalysis() { return concurrencyAnalysis; }
    public void setConcurrencyAnalysis(String concurrencyAnalysis) { this.concurrencyAnalysis = concurrencyAnalysis; }

    public String getCollectionAnalysis() { return collectionAnalysis; }
    public void setCollectionAnalysis(String collectionAnalysis) { this.collectionAnalysis = collectionAnalysis; }

    public String getGraphAnalysis() { return graphAnalysis; }
    public void setGraphAnalysis(String graphAnalysis) { this.graphAnalysis = graphAnalysis; }

    public String getRuntimeAnalysis() { return runtimeAnalysis; }
    public void setRuntimeAnalysis(String runtimeAnalysis) { this.runtimeAnalysis = runtimeAnalysis; }
    public int getReadabilityScore() { return readabilityScore; }
    public void setReadabilityScore(int readabilityScore) { this.readabilityScore = readabilityScore; }
    public int getMaintainabilityScore() { return maintainabilityScore; }
    public void setMaintainabilityScore(int maintainabilityScore) { this.maintainabilityScore = maintainabilityScore; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public CodeFile getCodeFile() { return codeFile; }
    public void setCodeFile(CodeFile codeFile) { this.codeFile = codeFile; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }
}
