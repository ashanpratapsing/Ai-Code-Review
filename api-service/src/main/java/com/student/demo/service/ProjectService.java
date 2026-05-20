package com.student.demo.service;

import com.student.demo.dto.ProjectDTO;
import com.student.demo.entity.Project;
import com.student.demo.entity.User;
import com.student.demo.exception.ForbiddenException;
import com.student.demo.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project, User user) {
        project.setUser(user);
        logger.info("Creating project '{}' for user {}", project.getProjectName(), user.getId());
        return projectRepository.save(project);
    }

    public List<ProjectDTO> getProjectsForUser(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(project -> new ProjectDTO(project.getId(), project.getProjectName(), project.getDescription()))
                .toList();
    }

    public void deleteProject(Long id, Long userId) {
        Project project = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ForbiddenException("Project not found or access denied"));
        logger.info("Deleting project {} for user {}", id, userId);
        projectRepository.delete(project);
    }

    public Project updateProject(Long id, Project details, Long userId) {
        Project project = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ForbiddenException("Project not found or access denied"));
        project.setProjectName(details.getProjectName());
        project.setDescription(details.getDescription());
        return projectRepository.save(project);
    }
}
