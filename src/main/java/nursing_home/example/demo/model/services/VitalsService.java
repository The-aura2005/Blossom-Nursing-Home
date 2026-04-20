package nursing_home.example.demo.model.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.dao.VitalsRepository;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.model.Vitals;

@Service
public class VitalsService {
    private final VitalsRepository vitalsRepository;
    private final ResidentRepository residentRepository;

    public VitalsService(VitalsRepository vitalsRepository, ResidentRepository residentRepository) {
        this.vitalsRepository = vitalsRepository;
        this.residentRepository = residentRepository;
    }

    public void addVitals(Long residentId, int temperature, String bloodPressure, int weight, String notes,
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
        vitalsRepository.save(vitals);
    }

    public List<Vitals> getAllVitals() {
        return vitalsRepository.findAllByOrderByDateRecordedDescIdDesc();
    }

    public List<Vitals> getVitalsForResident(Long residentId) {
        return vitalsRepository.findByResidentIdOrderByDateRecordedDescIdDesc(residentId);
    }

    public List<Vitals> getVitalsForResidents(List<Long> residentIds) {
        if (residentIds == null || residentIds.isEmpty()) {
            return List.of();
        }
        return vitalsRepository.findByResidentIdInOrderByDateRecordedDescIdDesc(residentIds);
    }

    public List<Vitals> getVitalsLoggedBy(String username) {
        return vitalsRepository.findByRecordedByUsernameOrderByDateRecordedDescIdDesc(username);
    }

}
