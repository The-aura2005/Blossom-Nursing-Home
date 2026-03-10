package nursing_home.example.demo.config;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.dao.StaffRepository;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;
import nursing_home.example.demo.model.Staff;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NursingHomeUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StaffRepository staffRepository;

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

            System.out.println("Test users initialized successfully!");
            System.out.println("Admin - Username: admin, Password: admin123");
            System.out.println("Staff - Username: staff, Password: staff123");
        }

        backfillMissingStaffUsers();
    }

    private void backfillMissingStaffUsers() {
        List<Staff> staffRecords = staffRepository.findAll();
        if (staffRecords.isEmpty()) {
            return;
        }

        long staffUsersCount = userRepository.findByNursingHomeUserRole(NursingHomeUserRole.STAFF).size();
        long missingAccounts = staffRecords.size() - staffUsersCount;
        if (missingAccounts <= 0) {
            return;
        }

        int created = 0;
        for (Staff staff : staffRecords) {
            if (created >= missingAccounts) {
                break;
            }

            String username = generateUniqueUsername(staff);
            String temporaryPassword = buildTemporaryPassword(staff);

            NursingHomeUser user = new NursingHomeUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(temporaryPassword));
            user.setNursingHomeUserRole(NursingHomeUserRole.STAFF);
            userRepository.save(user);
            created++;

            System.out.println("Backfilled staff user account for staff id " + staff.getId()
                    + " -> username: " + username + " temporary password: " + temporaryPassword);
        }
    }

    private String generateUniqueUsername(Staff staff) {
        String baseUsername = "staff";

        if (staff.getEmail() != null && staff.getEmail().contains("@")) {
            baseUsername = staff.getEmail().substring(0, staff.getEmail().indexOf('@'));
        } else if (staff.getName() != null && !staff.getName().isBlank()) {
            baseUsername = staff.getName().trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }

        if (baseUsername.isBlank()) {
            baseUsername = "staff";
        }

        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username) != null) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    private String buildTemporaryPassword(Staff staff) {
        int phoneTail = Math.abs(staff.getPhoneNumber()) % 10000;
        return "Staff@" + String.format("%04d", phoneTail);
    }
}
