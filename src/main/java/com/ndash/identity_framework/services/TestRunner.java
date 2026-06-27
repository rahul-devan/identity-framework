package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.repositories.UserApplicationRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserApplicationRepository userApplicationRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        try {

            List<User> users = userRepository.findAll();

            Role role = roleRepository.findByName("user").orElse(null);

            users.stream()
                    .filter(User::isActive)
                    .forEach(user ->
                    {

                    });

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
