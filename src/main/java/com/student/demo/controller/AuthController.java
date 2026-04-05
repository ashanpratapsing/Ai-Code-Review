package com.student.demo.controller;

import com.student.demo.entity.User;
import com.student.demo.repository.UserRepository;
import com.student.demo.security.JwtUtil;
import com.student.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {

        User existingUser = userRepository.findByEmail(user.getEmail()).orElseThrow();

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(existingUser.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return response;
    }
}