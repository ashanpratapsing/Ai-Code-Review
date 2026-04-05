package com.student.demo.dto;

public class ProjectDTO {

    private Long id;
    private String projectName;
    private String description;

    public ProjectDTO() {}

    public ProjectDTO(Long id, String projectName, String description) {
        this.id = id;
        this.projectName = projectName;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getProjectName() { return projectName; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public void setDescription(String description) { this.description = description; }
}