package com.ndash.identity_framework.config;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
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
