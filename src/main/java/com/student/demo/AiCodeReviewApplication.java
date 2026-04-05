package com.student.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.student.demo.repository")
@EntityScan(basePackages = "com.student.demo.entity")
public class AiCodeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewApplication.class, args);
    }
}