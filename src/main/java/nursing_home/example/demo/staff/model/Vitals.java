package nursing_home.example.demo.staff.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import nursing_home.example.demo.admin.Model.Resident;

@Entity
@Table(name = "Vitals")
public class Vitals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double temperature;
    private String bloodPressure;
    private int weight;
    private LocalDate dateRecorded;
    private String notes;
    private String recordedByUsername;
    @ManyToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;
    @Transient
    private String alertStatus = "NORMAL";
    @Transient
    private String alertMessage = "No abnormal values detected.";
    @Transient
    private List<String> alertMessages = List.of();

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

    public Vitals(double temperature, String bloodPressure, int weight, String notes) {
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.weight = weight;
        this.notes = notes;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
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

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public List<String> getAlertMessages() {
        return alertMessages;
    }

    public void setAlertMessages(List<String> alertMessages) {
        this.alertMessages = alertMessages == null ? List.of() : List.copyOf(alertMessages);
    }

}
