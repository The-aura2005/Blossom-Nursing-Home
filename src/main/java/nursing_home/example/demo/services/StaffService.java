package nursing_home.example.demo.services;

import java.util.List;
import java.util.Optional;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.dao.StaffRepository;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;
import nursing_home.example.demo.model.Staff;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final NursingHomeUserRepository nursingHomeUserRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository, NursingHomeUserRepository nursingHomeUserRepository,
            PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.nursingHomeUserRepository = nursingHomeUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public StaffCreationResult addStaff(Staff staff) {
        if (staff.getId() != null) {
            Optional<Staff> existingStaff = staffRepository.findById(staff.getId());

            if (existingStaff.isPresent()) {
                throw new IllegalStateException("Staff exists");
            }
        }
        Staff savedStaff = staffRepository.save(staff);

        String username = generateUniqueUsername(savedStaff);
        String temporaryPassword = buildTemporaryPassword(savedStaff);

        NursingHomeUser user = new NursingHomeUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setNursingHomeUserRole(NursingHomeUserRole.STAFF);
        nursingHomeUserRepository.save(user);

        return new StaffCreationResult(savedStaff, username, temporaryPassword);

    }

    // method to view staff
    public List<Staff> viewStaffs() {
        List<Staff> staffs = staffRepository.findAll();
        if (staffs.isEmpty()) {
            throw new IllegalStateException("no staff found");
        }
        return staffs;
    }

    // method to delete staff
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }

    // method to get staff by id
    public Staff getStaffById(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> new IllegalStateException("Staff not found"));
    }

    // method to update staff
    public void updateStaff(Staff staff) {
        Optional<Staff> existingStaff = staffRepository.findById(staff.getId());
        if (existingStaff.isPresent()) {
            Staff updatedStaff = existingStaff.get();
            updatedStaff.setName(staff.getName());
            updatedStaff.setPhoneNumber(staff.getPhoneNumber());
            updatedStaff.setEmail(staff.getEmail());
            updatedStaff.setStatus(staff.getStatus());
            staffRepository.save(updatedStaff);
        } else {
            throw new IllegalStateException("Staff does not exist");
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
        while (nursingHomeUserRepository.findByUsername(username) != null) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    private String buildTemporaryPassword(Staff staff) {
        int phoneTail = Math.abs(staff.getPhoneNumber()) % 10000;
        return "Staff@" + String.format("%04d", phoneTail);
    }

    public record StaffCreationResult(Staff staff, String username, String temporaryPassword) {
    }

}
