package com.student.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.student.demo")
@EnableJpaRepositories(basePackages = "com.student.demo.repository")
@EntityScan(basePackages = "com.student.demo.entity")
@org.springframework.amqp.rabbit.annotation.EnableRabbit
@EnableMethodSecurity
@org.springframework.scheduling.annotation.EnableScheduling
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
