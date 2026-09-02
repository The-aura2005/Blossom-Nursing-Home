package nursing_home.example.demo.admin.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.admin.Services.AdminSettingsService;
import nursing_home.example.demo.staff.Repository.ActivityLogRepository;
import nursing_home.example.demo.staff.Repository.MedicationAdministrationRepository;
import nursing_home.example.demo.staff.Repository.ResidentMedicalConditionRepository;
import nursing_home.example.demo.staff.Repository.StaffRepository;
import nursing_home.example.demo.staff.Repository.VitalsRepository;
import nursing_home.example.demo.staff.model.ActivityLog;
import nursing_home.example.demo.staff.model.MedicationAdministration;
import nursing_home.example.demo.staff.model.Staff;
import nursing_home.example.demo.staff.model.Vitals;

//controller helps to display pages of the application
@Controller
public class AdminPageController {
        // dependency injection of repositories to access data from the database
        private final ResidentRepository residentRepository;
        private final StaffRepository staffRepository;
        private final ActivityLogRepository activityLogRepository;
        private final VitalsRepository vitalsRepository;
        private final MedicationAdministrationRepository medicationAdministrationRepository;
        private final ResidentMedicalConditionRepository residentMedicalConditionRepository;
        private final AdminSettingsService adminSettingsService;

        public AdminPageController(ResidentRepository residentRepository, StaffRepository staffRepository,
                        ActivityLogRepository activityLogRepository, VitalsRepository vitalsRepository,
                        MedicationAdministrationRepository medicationAdministrationRepository,
                        ResidentMedicalConditionRepository residentMedicalConditionRepository,
                        AdminSettingsService adminSettingsService) {
                this.residentRepository = residentRepository;
                this.staffRepository = staffRepository;
                this.activityLogRepository = activityLogRepository;
                this.vitalsRepository = vitalsRepository;
                this.medicationAdministrationRepository = medicationAdministrationRepository;
                this.residentMedicalConditionRepository = residentMedicalConditionRepository;
                this.adminSettingsService = adminSettingsService;
        }

        @GetMapping("/admin-dashboard")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminDashboard(Model model) {
                // creates list of recentResidents and stream through to the following functions
                List<Resident> recentResidents = residentRepository.findAll().stream()
                                .sorted(Comparator.comparing(Resident::getAdmissionDate,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(6)
                                .toList();

                List<ActivityLog> recentActivities = activityLogRepository.findActivityHistoryWithResident().stream()
                                .limit(6)
                                .toList();

                List<Staff> staffInSystem = staffRepository.findAll().stream()
                                .sorted(Comparator.comparing(Staff::getId, Comparator.reverseOrder()))
                                .limit(6).toList();

                model.addAttribute("residentsCount", residentRepository.count());
                model.addAttribute("staffCount", staffRepository.count());
                model.addAttribute("healthReportsCount", vitalsRepository.count()
                                + medicationAdministrationRepository.count()
                                + residentMedicalConditionRepository.count());
                model.addAttribute("recentResidents", recentResidents);
                model.addAttribute("staffInSystem", staffInSystem);
                model.addAttribute("recentActivities", recentActivities);
                return "admin-dashboard";
        }

        @GetMapping("/admin-settings")
        @PreAuthorize("hasRole('ADMIN')")
        public String settingsPage(Authentication authentication, Model model) {
                model.addAttribute("adminUser", adminSettingsService.getUser(authentication.getName()));
                return "settings";
        }

        @PostMapping("/admin-settings/profile")
        @PreAuthorize("hasRole('ADMIN')")
        public String saveProfile(Authentication authentication, @RequestParam String displayName,
                        @RequestParam String email, @RequestParam String phone,
                        RedirectAttributes redirectAttributes) {
                adminSettingsService.updateProfile(authentication.getName(), displayName, email, phone);
                redirectAttributes.addFlashAttribute("settingsSuccess", "Profile saved successfully.");
                return "redirect:/admin-settings";
        }

        @PostMapping("/admin-settings/password")
        @PreAuthorize("hasRole('ADMIN')")
        public String updatePassword(Authentication authentication, @RequestParam String currentPassword,
                        @RequestParam String newPassword, @RequestParam String confirmPassword,
                        RedirectAttributes redirectAttributes) {
                try {
                        adminSettingsService.updatePassword(authentication.getName(), currentPassword, newPassword,
                                        confirmPassword);
                        redirectAttributes.addFlashAttribute("settingsSuccess", "Password updated successfully.");
                } catch (IllegalArgumentException exception) {
                        redirectAttributes.addFlashAttribute("settingsError", exception.getMessage());
                }
                return "redirect:/admin-settings";
        }

        @PostMapping("/admin-settings/notifications")
        @PreAuthorize("hasRole('ADMIN')")
        public String saveNotifications(Authentication authentication,
                        @RequestParam(defaultValue = "false") boolean residentAdmissionAlerts,
                        @RequestParam(defaultValue = "false") boolean staffTaskAlerts,
                        @RequestParam(defaultValue = "false") boolean dailyRevenueSummary,
                        RedirectAttributes redirectAttributes) {
                adminSettingsService.updateNotifications(authentication.getName(), residentAdmissionAlerts,
                                staffTaskAlerts, dailyRevenueSummary);
                redirectAttributes.addFlashAttribute("settingsSuccess", "Notification settings saved successfully.");
                return "redirect:/admin-settings";
        }

        @GetMapping("/reports")
        @PreAuthorize("hasAnyRole('ADMIN')")
        public String reportsPage(Model model) {
                List<Resident> latestResidentsAdded = residentRepository.findAll().stream()
                                .sorted(Comparator.comparing(Resident::getAdmissionDate,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(6)
                                .toList();

                List<ActivityLog> latestActivityReports = activityLogRepository.findActivityHistoryWithResident()
                                .stream()
                                .limit(6)
                                .toList();

                List<Vitals> latestVitalsReports = vitalsRepository.findAllByOrderByDateRecordedDescIdDesc().stream()
                                .limit(6)
                                .toList();

                List<MedicationAdministration> latestMedicationReports = medicationAdministrationRepository
                                .findAllWithResidentOrderByAdministeredAtDescIdDesc().stream()
                                .limit(6)
                                .toList();

                long medicalReportsCount = residentMedicalConditionRepository.count();
                long activityLogsCount = activityLogRepository.count();
                long vitalReportsCount = vitalsRepository.count();
                long medicationReportsCount = medicationAdministrationRepository.count();

                model.addAttribute("latestResidentsAdded", latestResidentsAdded);
                model.addAttribute("latestActivityReports", latestActivityReports);
                model.addAttribute("latestVitalsReports", latestVitalsReports);
                model.addAttribute("latestMedicationReports", latestMedicationReports);
                model.addAttribute("medicalReportsCount", medicalReportsCount);
                model.addAttribute("activityLogsCount", activityLogsCount);
                model.addAttribute("vitalReportsCount", vitalReportsCount);
                model.addAttribute("medicationReportsCount", medicationReportsCount);
                return "reports";

        }

        @GetMapping("/billingreports")
        @PreAuthorize("hasAnyRole('ADMIN')")
        public String billingReportsAliasPage() {
                return "redirect:/reports";
        }

        @GetMapping("/settings")
        @PreAuthorize("hasRole('ADMIN')")
        public String settingsAliasPage() {
                return "redirect:/admin-settings";
        }
}
