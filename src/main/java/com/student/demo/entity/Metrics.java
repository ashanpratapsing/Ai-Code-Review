package com.student.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "metrics")
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int linesOfCode;
    private int numberOfFunctions;
    private int numberOfLoops;
    private int nestedLoops;
    private int complexityScore;

    @Column(columnDefinition = "TEXT")
    private String summary;

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

    @OneToOne
    @JoinColumn(name = "code_file_id")
    private CodeFile codeFile;

    public Metrics() {}

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
    public CodeFile getCodeFile() { return codeFile; }
    public void setCodeFile(CodeFile codeFile) { this.codeFile = codeFile; }
}
