package nursing_home.example.demo.services;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.dao.VitalsRepository;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.model.Vitals;

@Service
@AllArgsConstructor
public class VitalsService {
    private VitalsRepository vitalsRepository;
    private ResidentRepository residentRepository;

    public void addVitals(Vitals vitals, Long residentId) {
        Resident existingResident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));
        vitals.setResident(existingResident);
        vitalsRepository.save(vitals);

    }

}
