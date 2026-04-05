package com.student.demo.repository;

import com.student.demo.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {
    List<CodeFile> findByProjectId(Long projectId);
}