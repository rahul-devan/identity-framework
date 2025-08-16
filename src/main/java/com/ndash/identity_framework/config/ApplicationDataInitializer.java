package com.ndash.identity_framework.config;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3) // runs after RoleDataInitializer
@Slf4j
public class ApplicationDataInitializer implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;

    public ApplicationDataInitializer(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createApplicationIfNotExists("JIRA", "Project management & issue tracking", "https://jira.example.com");
        createApplicationIfNotExists("Confluence", "Documentation & collaboration tool", "https://confluence.example.com");
        createApplicationIfNotExists("AWS Console", "Cloud platform access", "https://aws.amazon.com/console/");
        createApplicationIfNotExists("GitHub", "Source code management", "https://github.com");
        log.info("ApplicationDataInitializer:: Default Applications created");
    }

    private void createApplicationIfNotExists(String name, String description, String url) {
        boolean exists = applicationRepository.existsByName(name);
        if (!exists) {
            Application app = Application.builder()
                    .name(name)
                    .description(description)
                    .appUrl(url)
                    .active(true)
                    .build();
            applicationRepository.save(app);
            log.info("Created default application: {}", name);
        }
    }
}

