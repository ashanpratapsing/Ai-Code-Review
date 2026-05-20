package com.student.demo.controller;

import com.student.demo.dto.ProjectDTO;
import com.student.demo.entity.Project;
import com.student.demo.entity.User;
import com.student.demo.security.SecurityUtil;
import com.student.demo.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final SecurityUtil securityUtil;

    public ProjectController(ProjectService projectService, SecurityUtil securityUtil) {
        this.projectService = projectService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {
        User user = securityUtil.requireCurrentUser();
        return projectService.createProject(project, user);
    }

    @GetMapping
    public List<ProjectDTO> getAllProjects() {
        return projectService.getProjectsForUser(securityUtil.requireCurrentUserId());
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id, securityUtil.requireCurrentUserId());
    }

    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody Project project) {
        return projectService.updateProject(id, project, securityUtil.requireCurrentUserId());
    }
}
