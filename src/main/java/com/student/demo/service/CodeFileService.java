package com.student.demo.service;

import com.student.demo.dto.CodeFileDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.CodeFileRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeFileService {

    @Autowired
    private CodeFileRepository codeFileRepository;

    public CodeFile saveFile(CodeFile codeFile) {
        return codeFileRepository.save(codeFile);
    }
    public List<CodeFileDTO> getAllFiles() {
        List<CodeFile> files = codeFileRepository.findAll();

        return files.stream()
                .map(file -> new CodeFileDTO(
                        file.getId(),
                        file.getFileName(),
                        file.getLanguage()
                ))
                .toList();
    }
   
    
}