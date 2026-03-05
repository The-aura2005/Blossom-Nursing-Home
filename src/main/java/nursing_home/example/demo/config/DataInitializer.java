package nursing_home.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NursingHomeUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Migrate legacy plain-text passwords to BCrypt
        userRepository.findAll().forEach(user -> {
            String currentPassword = user.getPassword();
            if (currentPassword != null && !currentPassword.startsWith("$2a$")
                    && !currentPassword.startsWith("$2b$")
                    && !currentPassword.startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(currentPassword));
                userRepository.save(user);
            }
        });

        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            // Create Admin user
            NursingHomeUser admin = new NursingHomeUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNursingHomeUserRole(NursingHomeUserRole.ADMIN);
            userRepository.save(admin);

            // Create Staff user
            NursingHomeUser staff = new NursingHomeUser();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setNursingHomeUserRole(NursingHomeUserRole.STAFF);
            userRepository.save(staff);

            // Create Accountant user
            NursingHomeUser accountant = new NursingHomeUser();
            accountant.setUsername("accountant");
            accountant.setPassword(passwordEncoder.encode("accountant123"));
            accountant.setNursingHomeUserRole(NursingHomeUserRole.ACCOUNTANT);
            userRepository.save(accountant);

            System.out.println("Test users initialized successfully!");
            System.out.println("Admin - Username: admin, Password: admin123");
            System.out.println("Staff - Username: staff, Password: staff123");
            System.out.println("Accountant - Username: accountant, Password: accountant123");
        }
    }
}
