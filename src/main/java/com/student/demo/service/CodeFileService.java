package com.student.demo.service;

import com.student.demo.dto.CodeFileDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.CodeFileRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeFileService {

    private static final Logger logger = LoggerFactory.getLogger(CodeFileService.class);

    @Autowired
    private CodeFileRepository codeFileRepository;

    public CodeFile saveFile(CodeFile codeFile) {
        logger.info("Saving code file {}", codeFile.getFileName());

        if (codeFile.getCodeContent() == null) {
            throw new RuntimeException("Code content cannot be null");
        }

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