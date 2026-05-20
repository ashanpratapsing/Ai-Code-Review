package com.student.demo.repository;

import com.student.demo.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {

    List<CodeFile> findByProjectId(Long projectId);

    List<CodeFile> findByOrganizationId(java.util.UUID organizationId);

    @Query("SELECT cf FROM CodeFile cf JOIN cf.project p WHERE p.user.id = :userId ORDER BY cf.uploadedAt DESC")
    List<CodeFile> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT cf FROM CodeFile cf JOIN cf.project p WHERE cf.id = :id AND p.user.id = :userId")
    Optional<CodeFile> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT cf FROM CodeFile cf JOIN cf.project p WHERE cf.project.id = :projectId AND p.user.id = :userId ORDER BY cf.uploadedAt DESC")
    List<CodeFile> findByProjectIdForUser(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query("SELECT COUNT(cf) FROM CodeFile cf JOIN cf.project p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
