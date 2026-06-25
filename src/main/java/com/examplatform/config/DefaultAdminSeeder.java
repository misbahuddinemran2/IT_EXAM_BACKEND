package com.examplatform.config;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.entity.AdminUser.AdminRole;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAdminSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (adminUserRepository.existsByUsername("superadmin")) {
            log.info("Super Admin already exists");
            return;
        }

        AdminUser admin = AdminUser.builder()
                .username("superadmin")
                .email("superadmin@example.com")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .fullName("Super Admin")
                .role(AdminRole.SUPER_ADMIN)
                .isActive(true)
                .build();

        adminUserRepository.save(admin);

        log.info("=================================");
        log.info("DEFAULT SUPER ADMIN CREATED");
        log.info("Username: superadmin");
        log.info("Password: Admin@1234");
        log.info("=================================");
    }
}