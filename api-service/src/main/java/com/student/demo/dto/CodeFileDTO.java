package com.student.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeFileDTO {
    private Long id;
    private String name;
    private String content;
    private String language;
}
