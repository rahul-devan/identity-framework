package com.ndash.identity_framework.config;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.repositories.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class RoleDataInitializer implements CommandLineRunner {


    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("super_admin");
        createRoleIfNotExists("admin");
        createRoleIfNotExists("user");
        log.info("RoleDataInitializer:: Default Roles created");
    }


    private void createRoleIfNotExists(String roleName) {
        boolean exists = roleRepository.existsByName(roleName);
        if (!exists) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
