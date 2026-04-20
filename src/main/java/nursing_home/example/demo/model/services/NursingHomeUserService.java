package nursing_home.example.demo.model.services;

import org.springframework.stereotype.Service;

import java.util.Collections;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.model.NursingHomeUser;

@Service
@AllArgsConstructor
public class NursingHomeUserService implements UserDetailsService{
    @Autowired
    private NursingHomeUserRepository nursingHomeUserRepository;

     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NursingHomeUser user = nursingHomeUserRepository.findByUsername(username);
        if(user == null){
                throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return user;
}

}
