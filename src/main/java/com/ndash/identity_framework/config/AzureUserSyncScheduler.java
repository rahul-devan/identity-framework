package com.ndash.identity_framework.config;

import com.ndash.identity_framework.services.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AzureUserSyncScheduler {

    private final UserService userService;

    public AzureUserSyncScheduler(UserService userService) {
        this.userService = userService;
    }

    // Every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void syncUsers() {
        userService.syncUsersFromAzure();
    }
}
