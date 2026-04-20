package nursing_home.example.demo.model.services;

import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.IncidentReportRepository;
import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.model.IncidentReport;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.Resident;

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
        return incidentReportRepository.findAll();
    }

    public List<IncidentReport> getReportsByResident(Long residentId) {
        return incidentReportRepository.findByResidentIdOrderByCreatedAtDesc(residentId);
    }

    public List<IncidentReport> getReportsByStaff(Long staffId) {
        return incidentReportRepository.findByStaffIdOrderByCreatedAtDesc(staffId);
    }

    public List<IncidentReport> getReportsByStaffUsername(String username) {
        NursingHomeUser staff = nursingHomeUserRepository.findByUsername(username);
        if (staff == null) {
            return List.of();
        }
        return getReportsByStaff(staff.getId());
    }
}
