package nursing_home.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.dao.StaffRepository;
import nursing_home.example.demo.model.Staff;

@Service
@AllArgsConstructor
public class StaffService {
    private StaffRepository staffRepository;
    public void addStaff(Staff staff){
        if (staff.getId() != null){
        Optional<Staff> existingStaff = staffRepository.findById(staff.getId());
        
        if(existingStaff.isPresent()){
            throw new IllegalStateException("Staff exists");
        }
    }
        staffRepository.save(staff);
    
    }
    //method to view staff
    public List<Staff> viewStaffs(){
        return staffRepository.findAll();
    }  
    //method to delete staff
    public void deleteStaff(Long id){
        staffRepository.deleteById(id);
    }
    //method to get staff by id
    public Staff getStaffById(Long id){
        return staffRepository.findById(id).orElseThrow(() -> new IllegalStateException("Staff not found"));
    }
    //method to update staff
    public void updateStaff(Staff staff){
        Optional<Staff> existingStaff = staffRepository.findById(staff.getId());
        if (existingStaff.isPresent()) {
            Staff updatedStaff = existingStaff.get();
            updatedStaff.setName(staff.getName());
            updatedStaff.setPhoneNumber(staff.getPhoneNumber());
            updatedStaff.setEmail(staff.getEmail());
            updatedStaff.setStatus(staff.getStatus());
            staffRepository.save(updatedStaff);
        } else {
            throw new IllegalStateException("Staff does not exist");
        }
    }
    
}
