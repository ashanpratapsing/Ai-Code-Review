package com.student.demo.repository;

import com.student.demo.entity.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    Optional<Metrics> findByCodeFileId(Long codeFileId);
}