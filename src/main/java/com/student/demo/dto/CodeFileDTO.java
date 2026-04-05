package com.student.demo.dto;

public class CodeFileDTO {

    private Long id;
    private String fileName;
    private String language;

    public CodeFileDTO(Long id, String fileName, String language) {
        this.id = id;
        this.fileName = fileName;
        this.language = language;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getLanguage() { return language; }
}