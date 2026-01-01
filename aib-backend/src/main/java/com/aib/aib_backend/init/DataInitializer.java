package com.aib.aib_backend.init;



import com.aib.aib_backend.model.Role;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.repository.RoleRepository;
import com.aib.aib_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;  // ADD THIS

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional  // ADD THIS
    public void run(String... args) throws Exception {
        initializeRoles();
        createAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Regular user")
                    .build();
            roleRepository.save(userRole);

            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator")
                    .build();
            roleRepository.save(adminRole);

            Role techRole = Role.builder()
                    .name("ROLE_TECHNICIAN")
                    .description("Repair technician")
                    .build();
            roleRepository.save(techRole);

            System.out.println("✅ Roles initialized");
        }
    }

    private void createAdminUser() {
        if (!userRepository.existsByEmail("admin@aib.com")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow();

            User admin = User.builder()
                    .email("admin@aib.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phone("+21612345678")
                    .emailVerified(true)
                    .enabled(true)
                    .provider("LOCAL")
                    .build();

            admin.getRoles().add(adminRole);  
            userRepository.save(admin);

            System.out.println("✅ Admin user created: admin@aib.com / admin123");
        }
    }
}