package com.student.demo.repository;

import com.student.demo.entity.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<AnalysisHistory, Long> {

    List<AnalysisHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AnalysisHistory> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT h FROM AnalysisHistory h WHERE h.user.id = :userId AND h.createdAt >= :since ORDER BY h.createdAt DESC")
    List<AnalysisHistory> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
