package nursing_home.example.demo.staff.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.NursingHomeUser;
import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.NursingHomeUserRepository;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.IncidentReportRepository;
import nursing_home.example.demo.staff.model.IncidentReport;

@Service
public class IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final ResidentRepository residentRepository;
    private final NursingHomeUserRepository nursingHomeUserRepository;

    public IncidentReportService(
            IncidentReportRepository incidentReportRepository,
            ResidentRepository residentRepository,
            NursingHomeUserRepository nursingHomeUserRepository) {
        this.incidentReportRepository = incidentReportRepository;
        this.residentRepository = residentRepository;
        this.nursingHomeUserRepository = nursingHomeUserRepository;
    }

    public IncidentReport saveReport(Long residentId, String username, String title, String description,
            String severity) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        NursingHomeUser staff = nursingHomeUserRepository.findByUsername(username);
        if (staff == null) {
            throw new IllegalStateException("Staff user not found");
        }

        IncidentReport report = new IncidentReport();
        report.setResident(resident);
        report.setStaff(staff);
        report.setTitle(title);
        report.setDescription(description);
        report.setSeverity(severity);
        return incidentReportRepository.save(report);
    }

    public List<IncidentReport> getAllReports() {
        return enrichResidents(incidentReportRepository.findAll());
    }

    public List<IncidentReport> getReportsByResident(Long residentId) {
        return enrichResidents(incidentReportRepository.findByResidentIdOrderByCreatedAtDesc(residentId));
    }

    public List<IncidentReport> getReportsByStaff(Long staffId) {
        return enrichResidents(incidentReportRepository.findByStaffIdOrderByCreatedAtDesc(staffId));
    }

    public List<IncidentReport> getReportsByStaffUsername(String username) {
        NursingHomeUser staff = nursingHomeUserRepository.findByUsername(username);
        if (staff == null) {
            return List.of();
        }
        return getReportsByStaff(staff.getId());
    }

    private List<IncidentReport> enrichResidents(List<IncidentReport> reports) {
        return reports.stream().map(report -> {
            if (report.getResident() != null && report.getResident().getId() != null) {
                residentRepository.findById(report.getResident().getId()).ifPresent(report::setResident);
            }
            return report;
        }).toList();
    }
}
