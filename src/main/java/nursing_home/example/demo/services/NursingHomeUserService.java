package nursing_home.example.demo.services;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;
@Service
public class NursingHomeUserService {
    
    @Autowired
    private  NursingHomeUserRepository nursingHomeUserRepository;

    public NursingHomeUserRole authenticate(String username, String password){
        Optional<NursingHomeUser> optionalUser = nursingHomeUserRepository.findByUsername(username);

        if(optionalUser.isPresent()){
            NursingHomeUser user = optionalUser.get();

            if (user.getPassword().equals(password)) {
                return user.getNursingHomeUserRole();
            }
        } 
        return null;

    }

}