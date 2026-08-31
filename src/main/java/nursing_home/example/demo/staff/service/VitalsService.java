package nursing_home.example.demo.staff.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.VitalsRepository;
import nursing_home.example.demo.staff.model.Vitals;

@Service
public class VitalsService {
    private final VitalsRepository vitalsRepository;
    private final ResidentRepository residentRepository;
    private final VitalsAlertEvaluator alertEvaluator;

    public VitalsService(VitalsRepository vitalsRepository, ResidentRepository residentRepository,
            VitalsAlertEvaluator alertEvaluator) {
        this.vitalsRepository = vitalsRepository;
        this.residentRepository = residentRepository;
        this.alertEvaluator = alertEvaluator;
    }

    public void addVitals(Long residentId, double temperature, String bloodPressure, int weight, String notes,
            String recordedByUsername) {
        Resident existingResident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        Vitals vitals = new Vitals();
        vitals.setTemperature(temperature);
        vitals.setBloodPressure(bloodPressure);
        vitals.setWeight(weight);
        vitals.setNotes(notes);
        vitals.setDateRecorded(LocalDate.now());
        vitals.setRecordedByUsername(recordedByUsername);
        vitals.setResident(existingResident);
        alertEvaluator.evaluate(vitals);
        vitalsRepository.save(vitals);
    }

    public List<Vitals> getAllVitals() {
        return evaluate(vitalsRepository.findAllByOrderByDateRecordedDescIdDesc());
    }

    public List<Vitals> getVitalsForResident(Long residentId) {
        return evaluate(vitalsRepository.findByResidentIdOrderByDateRecordedDescIdDesc(residentId));
    }

    public List<Vitals> getVitalsForResidents(List<Long> residentIds) {
        if (residentIds == null || residentIds.isEmpty()) {
            return List.of();
        }
        return evaluate(vitalsRepository.findByResidentIdInOrderByDateRecordedDescIdDesc(residentIds));
    }

    public List<Vitals> getVitalsLoggedBy(String username) {
        return evaluate(vitalsRepository.findByRecordedByUsernameOrderByDateRecordedDescIdDesc(username));
    }

    private List<Vitals> evaluate(List<Vitals> vitals) {
        vitals.forEach(vital -> {
            if (vital.getResident() != null && vital.getResident().getId() != null) {
                residentRepository.findById(vital.getResident().getId()).ifPresent(vital::setResident);
            }
            alertEvaluator.evaluate(vital);
        });
        return vitals;
    }

}
