package nursing_home.example.demo.admin.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nursing_home.example.demo.admin.Model.NursingHomeUser;
import nursing_home.example.demo.admin.Repository.NursingHomeUserRepository;

@Service
public class AdminSettingsService {
    private final NursingHomeUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSettingsService(NursingHomeUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public NursingHomeUser getUser(String username) {
        NursingHomeUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user;
    }

    @Transactional
    public void updateProfile(String username, String displayName, String email, String phone) {
        NursingHomeUser user = getUser(username);
        user.setDisplayName(displayName == null ? "" : displayName.trim());
        user.setEmail(email == null ? "" : email.trim());
        user.setPhone(phone == null ? "" : phone.trim());
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        NursingHomeUser user = getUser(username);
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateNotifications(String username, boolean residentAdmissionAlerts,
            boolean staffTaskAlerts, boolean dailyRevenueSummary) {
        NursingHomeUser user = getUser(username);
        user.setResidentAdmissionAlerts(residentAdmissionAlerts);
        user.setStaffTaskAlerts(staffTaskAlerts);
        user.setDailyRevenueSummary(dailyRevenueSummary);
        userRepository.save(user);
    }
}
