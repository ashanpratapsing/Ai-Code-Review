package com.student.demo.repository;

import com.student.demo.entity.CodeExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodeExecutionRepository extends JpaRepository<CodeExecution, Long> {

    Page<CodeExecution> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<CodeExecution> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT e FROM CodeExecution e WHERE e.user.id = :userId AND e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<CodeExecution> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
