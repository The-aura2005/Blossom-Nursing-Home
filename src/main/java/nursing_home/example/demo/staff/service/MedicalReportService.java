package nursing_home.example.demo.staff.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.ActivityLogRepository;
import nursing_home.example.demo.staff.Repository.MedicationAdministrationRepository;
import nursing_home.example.demo.staff.Repository.ResidentMedicalConditionRepository;
import nursing_home.example.demo.staff.Repository.VitalsRepository;
import nursing_home.example.demo.staff.model.ActivityLog;
import nursing_home.example.demo.staff.model.MedicationAdministration;
import nursing_home.example.demo.staff.model.ResidentMedicalCondition;
import nursing_home.example.demo.staff.model.Vitals;
import nursing_home.example.demo.staff.model.ResidentMedicalCondition.ConditionType;

@Service
public class MedicalReportService {

    private final ResidentRepository residentRepository;
    private final ResidentMedicalConditionRepository conditionRepository;
    private final MedicationAdministrationRepository medicationRepository;
    private final VitalsRepository vitalsRepository;
    private final ActivityLogRepository activityLogRepository;
    private final VitalsAlertEvaluator alertEvaluator;

    public MedicalReportService(
            ResidentRepository residentRepository,
            ResidentMedicalConditionRepository conditionRepository,
            MedicationAdministrationRepository medicationRepository,
            VitalsRepository vitalsRepository,
            ActivityLogRepository activityLogRepository,
            VitalsAlertEvaluator alertEvaluator) {
        this.residentRepository = residentRepository;
        this.conditionRepository = conditionRepository;
        this.medicationRepository = medicationRepository;
        this.vitalsRepository = vitalsRepository;
        this.activityLogRepository = activityLogRepository;
        this.alertEvaluator = alertEvaluator;
    }

    public List<Resident> getResidents() {
        return residentRepository.findAllByOrderByNameAsc();
    }

    public MedicalReportView buildReport(Long residentId, LocalDate fromDate, LocalDate toDate) {
        List<Resident> residents = getResidents();
        if (residents.isEmpty()) {
            return new MedicalReportView(null, null, null, Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                    new ReportSummary(0, 0, 0, 0), Collections.emptyMap(), "No residents found.");
        }

        Resident selectedResident = resolveResident(residentId, residents);
        LocalDate effectiveFrom = fromDate;
        LocalDate effectiveTo = toDate;

        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            LocalDate temp = effectiveFrom;
            effectiveFrom = effectiveTo;
            effectiveTo = temp;
        }

        List<ResidentMedicalCondition> conditions = conditionRepository
                .findByResidentIdOrderByConditionTypeAscDescriptionAsc(selectedResident.getId());

        List<String> chronicIllnesses = conditions.stream()
                .filter(c -> c.getConditionType() == ConditionType.CHRONIC_ILLNESS)
                .map(ResidentMedicalCondition::getDescription)
                .toList();

        List<String> allergies = conditions.stream()
                .filter(c -> c.getConditionType() == ConditionType.ALLERGY)
                .map(ResidentMedicalCondition::getDescription)
                .toList();

        List<MedicationAdministration> medications = fetchMedications(selectedResident.getId(), effectiveFrom,
                effectiveTo);
        List<Vitals> vitals = fetchVitals(selectedResident.getId(), effectiveFrom, effectiveTo);
        List<ActivityLog> activityLogs = fetchActivities(selectedResident.getId(), effectiveFrom, effectiveTo);

        vitals.forEach(alertEvaluator::evaluate);
        List<Vitals> abnormalVitals = vitals.stream().filter(vital -> !"NORMAL".equals(vital.getAlertStatus()))
                .toList();
        Map<String, Long> activitySummary = buildActivitySummary(activityLogs);

        ReportSummary summary = new ReportSummary(
                abnormalVitals.size(),
                activityLogs.size(),
                medications.size(),
                vitals.size());

        return new MedicalReportView(
                selectedResident,
                effectiveFrom,
                effectiveTo,
                chronicIllnesses,
                allergies,
                medications,
                vitals,
                abnormalVitals,
                activityLogs,
                summary,
                activitySummary,
                null);
    }

    public void addCondition(Long residentId, ConditionType conditionType, String description) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Condition description is required");
        }

        ResidentMedicalCondition condition = new ResidentMedicalCondition();
        condition.setResident(resident);
        condition.setConditionType(conditionType);
        condition.setDescription(description.trim());
        conditionRepository.save(condition);
    }

    public void addMedication(Long residentId, String medicationName, String dosage, String administeredByUsername,
            String notes) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        if (medicationName == null || medicationName.isBlank()) {
            throw new IllegalStateException("Medication name is required");
        }

        MedicationAdministration med = new MedicationAdministration();
        med.setResident(resident);
        med.setMedicationName(medicationName.trim());
        med.setDosage((dosage == null || dosage.isBlank()) ? "Not specified" : dosage.trim());
        med.setAdministeredByUsername(
                (administeredByUsername == null || administeredByUsername.isBlank()) ? "Unknown"
                        : administeredByUsername.trim());
        med.setNotes(notes);
        med.setAdministeredAt(LocalDateTime.now());
        medicationRepository.save(med);
    }

    private Resident resolveResident(Long residentId, List<Resident> residents) {
        if (residentId == null) {
            return residents.get(0);
        }

        return residents.stream()
                .filter(r -> residentId.equals(r.getId()))
                .findFirst()
                .orElse(residents.get(0));
    }

    private List<MedicationAdministration> fetchMedications(Long residentId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return medicationRepository.findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(
                    residentId,
                    fromDate.atStartOfDay(),
                    LocalDateTime.of(toDate, LocalTime.MAX));
        }
        return medicationRepository.findByResidentIdOrderByAdministeredAtDescIdDesc(residentId);
    }

    private List<Vitals> fetchVitals(Long residentId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return vitalsRepository.findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(residentId,
                    fromDate,
                    toDate);
        }
        return vitalsRepository.findByResidentIdOrderByDateRecordedDescIdDesc(residentId);
    }

    private List<ActivityLog> fetchActivities(Long residentId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return activityLogRepository
                    .findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
                            residentId, fromDate, toDate);
        }
        return activityLogRepository.findByResidentIdOrderByActivityDateDescActivityTimeDescIdDesc(residentId);
    }

    private Map<String, Long> buildActivitySummary(List<ActivityLog> activityLogs) {
        Map<String, Long> summary = new LinkedHashMap<>();

        activityLogs.stream()
                .map(ActivityLog::getActivityType)
                .filter(type -> type != null && !type.isBlank())
                .forEach(type -> summary.put(type, summary.getOrDefault(type, 0L) + 1));

        return summary.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    public record ReportSummary(long abnormalVitalsCount, long activitiesCount, long medicationsCount,
            long totalVitalsCount) {
    }

    public record MedicalReportView(
            Resident resident,
            LocalDate fromDate,
            LocalDate toDate,
            List<String> chronicIllnesses,
            List<String> allergies,
            List<MedicationAdministration> medications,
            List<Vitals> vitals,
            List<Vitals> abnormalVitals,
            List<ActivityLog> activityLogs,
            ReportSummary summary,
            Map<String, Long> activitySummary,
            String infoMessage) {
    }
}
