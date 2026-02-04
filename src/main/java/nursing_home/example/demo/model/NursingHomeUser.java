package nursing_home.example.demo.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class NursingHomeUser  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private NursingHomeUserRole nursingHomeUserRole;

    public NursingHomeUser(){}
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
    
    
}
