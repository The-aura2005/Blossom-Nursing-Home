package nursing_home.example.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.model.NursingHomeUser;

@Service
@AllArgsConstructor
public class NursingHomeUserService {
    @Autowired
    private NursingHomeUserRepository nursingHomeUserRepository;

    public NursingHomeUser login(String username, String password) {
        return nursingHomeUserRepository.findByUsername(username)
                .filter(NursingHomeUser -> NursingHomeUser.getPassword().equals(password))
                .orElse(null);

    }

}
