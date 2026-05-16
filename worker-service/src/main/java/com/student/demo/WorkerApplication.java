package com.student.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.student.demo")
@EnableJpaRepositories(basePackages = "com.student.demo.repository")
@EntityScan(basePackages = "com.student.demo.entity")
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public class WorkerApplication {
    public static void main(String[] args) {
        // Disable web server for worker
        SpringApplication app = new SpringApplication(WorkerApplication.class);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        app.run(args);
    }
}
