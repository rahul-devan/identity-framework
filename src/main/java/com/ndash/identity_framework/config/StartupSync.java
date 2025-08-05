package com.ndash.identity_framework.config;

import com.ndash.identity_framework.services.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class StartupSync {

    private final UserService userService;

    public StartupSync(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void initialSync() {
        userService.syncUsersFromAzure();
    }
}
