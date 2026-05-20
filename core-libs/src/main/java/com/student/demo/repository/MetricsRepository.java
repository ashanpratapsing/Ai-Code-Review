package com.student.demo.repository;

import com.student.demo.entity.AnalysisStatus;
import com.student.demo.entity.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MetricsRepository extends JpaRepository<Metrics, Long> {

    Optional<Metrics> findByCodeFileId(Long codeFileId);

    @Query("SELECT COUNT(m) FROM Metrics m JOIN m.codeFile cf JOIN cf.project p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Metrics m JOIN m.codeFile cf JOIN cf.project p WHERE p.user.id = :userId AND m.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AnalysisStatus status);

    @Query("SELECT m FROM Metrics m JOIN m.codeFile cf JOIN cf.project p WHERE m.codeFile.id = :fileId AND p.user.id = :userId")
    Optional<Metrics> findByCodeFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") Long userId);

    @Query("SELECT AVG(m.complexityScore) FROM Metrics m JOIN m.codeFile cf JOIN cf.project p WHERE p.user.id = :userId AND m.status = 'COMPLETED'")
    Double averageComplexityScoreByUserId(@Param("userId") Long userId);
}
