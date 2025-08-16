package com.ndash.identity_framework.config;

import com.ndash.identity_framework.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Slf4j
public class AzureDataInitializer implements CommandLineRunner {

    private final UserService userService;

    public AzureDataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        userService.syncUsersFromAzure();
        log.info("AzureDataInitializer:: Sync users from azure to database completed");
    }
}
