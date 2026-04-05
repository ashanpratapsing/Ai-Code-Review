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

    @OneToOne
    @JoinColumn(name = "code_file_id")
    private CodeFile codeFile;

    public Metrics() {}

    public Long getId() {
        return id;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public int getNumberOfFunctions() {
        return numberOfFunctions;
    }

    public void setNumberOfFunctions(int numberOfFunctions) {
        this.numberOfFunctions = numberOfFunctions;
    }

    public int getNumberOfLoops() {
        return numberOfLoops;
    }

    public void setNumberOfLoops(int numberOfLoops) {
        this.numberOfLoops = numberOfLoops;
    }

    public int getNestedLoops() {
        return nestedLoops;
    }

    public void setNestedLoops(int nestedLoops) {
        this.nestedLoops = nestedLoops;
    }

    public int getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(int complexityScore) {
        this.complexityScore = complexityScore;
    }

    public CodeFile getCodeFile() {
        return codeFile;
    }

    public void setCodeFile(CodeFile codeFile) {
        this.codeFile = codeFile;
    }
}
