package com.student.demo.repository;

import com.student.demo.entity.AIReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    Optional<AIReport> findByCodeFileId(Long codeFileId);
}
