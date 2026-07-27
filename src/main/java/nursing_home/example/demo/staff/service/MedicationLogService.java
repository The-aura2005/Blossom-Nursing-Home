package nursing_home.example.demo.staff.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.MedicationAdministrationRepository;
import nursing_home.example.demo.staff.model.MedicationAdministration;

@Service
public class MedicationLogService {

    private final MedicationAdministrationRepository medicationAdministrationRepository;
    private final ResidentRepository residentRepository;

    public MedicationLogService(
            MedicationAdministrationRepository medicationAdministrationRepository,
            ResidentRepository residentRepository) {
        this.medicationAdministrationRepository = medicationAdministrationRepository;
        this.residentRepository = residentRepository;
    }

    public MedicationAdministration saveMedicationLog(Long residentId, String username, String medicationName,
            String dosage,
            LocalDateTime timeGiven, String notes) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        MedicationAdministration medicationAdministration = new MedicationAdministration();
        medicationAdministration.setResident(resident);
        medicationAdministration.setMedicationName(medicationName);
        medicationAdministration.setDosage(dosage);
        medicationAdministration.setAdministeredByUsername(username);
        medicationAdministration.setAdministeredAt(timeGiven);
        medicationAdministration.setNotes(notes);
        return medicationAdministrationRepository.save(medicationAdministration);
    }

    public List<MedicationAdministration> getAllLogs() {
        return medicationAdministrationRepository.findAllWithResidentOrderByAdministeredAtDescIdDesc();
    }

    public List<MedicationAdministration> getLogsByResident(Long residentId) {
        return medicationAdministrationRepository.findByResidentIdOrderByAdministeredAtDescIdDesc(residentId);
    }

    public List<MedicationAdministration> getLogsByStaffUsername(String username) {
        return medicationAdministrationRepository.findByAdministeredByUsernameOrderByAdministeredAtDescIdDesc(
                username);
    }
}
