package com.sales.visits.app.sales.seed;

import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.model.enums.UserStatus;
import com.sales.visits.app.sales.repository.PermissionRepository;
import com.sales.visits.app.sales.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SuperAdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;

    @Value("${app.seed.super-admin.username}")
    private String username;

    @Value("${app.seed.super-admin.email}")
    private String email;

    @Value("${app.seed.super-admin.password}")
    private String password;

    public SuperAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean exists = userRepository.findByRole(UserRole.SUPER_ADMIN)
                .stream().findAny().isPresent();
        if(exists)
            return;

        Permission userManage = permissionRepository
                .findByCode("USER_MANAGE")
                .orElseThrow();

        Permission permissionAssign = permissionRepository
                .findByCode("PERMISSION_ASSIGN")
                .orElseThrow();
        Set<Permission> permissions = new HashSet<>();
        permissions.add(userManage);
        permissions.add(permissionAssign);

        User superAdmin = new User();
        superAdmin.setUsername(username);
        superAdmin.setEmail(email);
        superAdmin.setPasswordHash(passwordEncoder.encode(password));
        superAdmin.setRole(UserRole.SUPER_ADMIN);
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setPermissions(permissions);

        userRepository.save(superAdmin);
        log.info("Seeded SUPER_ADMIN user: " + email);
    }

}
