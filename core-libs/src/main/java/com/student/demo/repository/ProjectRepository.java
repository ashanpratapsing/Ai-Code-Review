package com.student.demo.repository;

import com.student.demo.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Project> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<Project> findByOrganizationId(java.util.UUID organizationId);
}
