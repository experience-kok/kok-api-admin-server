package com.example.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EnableAsync
@ConfigurationPropertiesScan
public class AdminserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminserviceApplication.class, args);
    }
}
