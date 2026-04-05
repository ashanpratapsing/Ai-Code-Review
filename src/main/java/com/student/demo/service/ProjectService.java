package com.student.demo.service;

import com.student.demo.dto.ProjectDTO;
import com.student.demo.entity.Project;
import com.student.demo.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    public Project createProject(Project project) {
        logger.info("Creating project: {}", project.getProjectName());
        return projectRepository.save(project);
    }

    public List<ProjectDTO> getAllProjects() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .map(project -> new ProjectDTO(
                        project.getId(),
                        project.getProjectName(),
                        project.getDescription()
                ))
                .toList();
    }
}