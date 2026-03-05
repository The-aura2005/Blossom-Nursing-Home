package nursing_home.example.demo.model;


import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class NursingHomeUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private NursingHomeUserRole nursingHomeUserRole;

    @Override
    public boolean isAccountNonExpired() {
       return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    public NursingHomeUser() {}
    
    public NursingHomeUser(String username, String password, NursingHomeUserRole nursingHomeUserRole) {
        this.username = username;
        this.password = password;
        this.nursingHomeUserRole = nursingHomeUserRole;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public NursingHomeUserRole getNursingHomeUserRole() {
        return nursingHomeUserRole;
    }
    public void setNursingHomeUserRole(NursingHomeUserRole nursingHomeUserRole) {
        this.nursingHomeUserRole = nursingHomeUserRole;
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return the user's role as a GrantedAuthority
          return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + nursingHomeUserRole.name()));
    
}
}
