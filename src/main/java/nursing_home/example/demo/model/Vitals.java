package nursing_home.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Vitals")
public class Vitals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int temperature;
    private String bloodPressure;
    private int weight;
    private LocalDate dateRecorded;
    private String notes;
    private String recordedByUsername;
    @ManyToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;

    public Vitals(LocalDate dateRecorded) {
        this.dateRecorded = dateRecorded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateRecorded() {
        return dateRecorded;
    }

    public void setDateRecorded(LocalDate dateRecorded) {
        this.dateRecorded = dateRecorded;
    }

    public Vitals() {

    }

    public Vitals(int temperature, String bloodPressure, int weight, String notes) {
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.weight = weight;
        this.notes = notes;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public Resident getResident() {
        return resident;
    }

    public String getRecordedByUsername() {
        return recordedByUsername;
    }

    public void setRecordedByUsername(String recordedByUsername) {
        this.recordedByUsername = recordedByUsername;
    }

}
