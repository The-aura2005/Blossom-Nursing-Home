package nursing_home.example.demo.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "residents")
public class Resident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int age;
    private String gender;
    private int roomNumber;
    private LocalDate admissionDate;
    private long EmergencyContact;
    private String status;
    @OneToMany(mappedBy = "resident" , cascade= CascadeType.ALL)
    private List<Vitals> vitals;

     public Resident(){}
    public Resident(String name, int age, String gender, int roomNumber, LocalDate admissionDate, String status,long EmergencyContact) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.roomNumber = roomNumber;
        this.admissionDate = admissionDate;
        this.EmergencyContact = EmergencyContact;
        this.status = status;
    }
    public long getEmergencyContact() {
        return EmergencyContact;
    }
    public void setEmergencyContact(long emergencyContact) {
        EmergencyContact = emergencyContact;
    }
    public Long getId() {
        return id;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public int getRoomNumber() {
        return roomNumber;
    }
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
    public LocalDate getAdmissionDate() {
        return admissionDate;
    }
    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }
   
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    
}
