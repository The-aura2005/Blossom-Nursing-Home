package nursing_home.example.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.model.Resident;

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
        List<Resident> residents = residentRepository.findAll();
        if (residents.isEmpty()) {
            throw new IllegalStateException("No residents found");
        } else {
            return residents;
        }

    }

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
            updatedResident.setStatus(resident.getStatus());
            residentRepository.save(updatedResident);
        } else {
            throw new IllegalStateException("Resident does not exist");
        }
    }

}
