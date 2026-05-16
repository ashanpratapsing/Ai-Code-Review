package com.student.demo.controller;

import com.student.demo.dto.ProjectDTO;
import com.student.demo.entity.Project;
import com.student.demo.entity.User;
import com.student.demo.repository.UserRepository;
import com.student.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProjectController.class);

    @PostMapping
    public Project createProject(@RequestBody Project project, Authentication authentication) {
        if (authentication == null) {
             throw new RuntimeException("User is not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> 
            new RuntimeException("Authenticated user not found in database: " + email));
        
        project.setUser(user);
        return projectService.createProject(project);
    }




    @GetMapping
    public List<ProjectDTO> getAllProjects(Authentication authentication) {
        return projectService.getAllProjects();
    }
}
