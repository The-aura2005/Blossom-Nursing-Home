package nursing_home.example.demo.controllers;

import java.util.Comparator;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import nursing_home.example.demo.dao.ActivityLogRepository;
import nursing_home.example.demo.dao.MedicationAdministrationRepository;
import nursing_home.example.demo.dao.ResidentMedicalConditionRepository;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.dao.StaffRepository;
import nursing_home.example.demo.dao.VitalsRepository;
import nursing_home.example.demo.model.ActivityLog;
import nursing_home.example.demo.model.MedicationAdministration;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.model.Staff;
import nursing_home.example.demo.model.Vitals;

@Controller
public class AdminPageController {

        private final ResidentRepository residentRepository;
        private final StaffRepository staffRepository;
        private final ActivityLogRepository activityLogRepository;
        private final VitalsRepository vitalsRepository;
        private final MedicationAdministrationRepository medicationAdministrationRepository;
        private final ResidentMedicalConditionRepository residentMedicalConditionRepository;

        public AdminPageController(
                        ResidentRepository residentRepository,
                        StaffRepository staffRepository,
                        ActivityLogRepository activityLogRepository,
                        VitalsRepository vitalsRepository,
                        MedicationAdministrationRepository medicationAdministrationRepository,
                        ResidentMedicalConditionRepository residentMedicalConditionRepository) {
                this.residentRepository = residentRepository;
                this.staffRepository = staffRepository;
                this.activityLogRepository = activityLogRepository;
                this.vitalsRepository = vitalsRepository;
                this.medicationAdministrationRepository = medicationAdministrationRepository;
                this.residentMedicalConditionRepository = residentMedicalConditionRepository;
        }

        @GetMapping("/admin-dashboard")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminDashboard(Model model) {
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
                                .limit(6)
                                .toList();

                long residentsCount = residentRepository.count();
                long staffCount = staffRepository.count();
                long healthReportsCount = vitalsRepository.count()
                                + medicationAdministrationRepository.count()
                                + residentMedicalConditionRepository.count();

                model.addAttribute("residentsCount", residentsCount);
                model.addAttribute("staffCount", staffCount);
                model.addAttribute("healthReportsCount", healthReportsCount);
                model.addAttribute("recentResidents", recentResidents);
                model.addAttribute("staffInSystem", staffInSystem);
                model.addAttribute("recentActivities", recentActivities);
                return "admin-dashboard";
        }

        @GetMapping("/admin-settings")
        @PreAuthorize("hasRole('ADMIN')")
        public String settingsPage() {
                return "settings";
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
