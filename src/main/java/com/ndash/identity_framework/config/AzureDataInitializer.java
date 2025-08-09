package com.ndash.identity_framework.config;

import com.ndash.identity_framework.services.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AzureDataInitializer implements CommandLineRunner {

    private final UserService userService;

    public AzureDataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        userService.syncUsersFromAzure();
    }
}
