package nursing_home.example.demo.admin.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;

@Service
public class ResidentService {
    private final ResidentRepository residentRepository;

    public ResidentService(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    public void addResident(Resident resident) {
        if (resident.getId() != null) {
            Optional<Resident> existingResident = residentRepository.findById(resident.getId());
            if (existingResident.isPresent()) {
                throw new IllegalStateException("Resident already exists");
            }
        }
        residentRepository.save(resident);
    }

    public List<Resident> viewResidents() {
        return residentRepository.findAll();
    }

    @Transactional
    public void deleteResident(Long id) {
        residentRepository.deleteById(id);
    }

    public Resident getResidentById(Long id) {
        return residentRepository.findById(id).orElse(null);
    }

    public void updateResident(Resident resident) {
        Optional<Resident> existingResident = residentRepository.findById(resident.getId());
        if (existingResident.isPresent()) {
            Resident updatedResident = existingResident.get();
            updatedResident.setName(resident.getName());
            updatedResident.setAge(resident.getAge());
            updatedResident.setRoomNumber(resident.getRoomNumber());
            updatedResident.setAdmissionDate(resident.getAdmissionDate());
            residentRepository.save(updatedResident);
        } else {
            throw new IllegalStateException("Resident does not exist");
        }
    }

}
