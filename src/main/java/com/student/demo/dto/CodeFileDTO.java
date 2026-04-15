package com.student.demo.dto;

public class CodeFileDTO {

    private Long id;
    private String fileName;
    private String language;
    private String codeContent;

    public CodeFileDTO(Long id, String fileName, String language, String codeContent) {
        this.id = id;
        this.fileName = fileName;
        this.language = language;
        this.codeContent = codeContent;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getLanguage() { return language; }
    public String getCodeContent() { return codeContent; }
}