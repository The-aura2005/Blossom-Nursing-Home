package nursing_home.example.demo.services;


import org.springframework.stereotype.Service;

import nursing_home.example.demo.model.NursingHomeUser;




@Service
public interface LoginUserService {
    NursingHomeUser Login(String username,String password);


}