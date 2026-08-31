package nursing_home.example.demo.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.ActivityLogRepository;
import nursing_home.example.demo.staff.Repository.IncidentReportRepository;
import nursing_home.example.demo.staff.Repository.MedicationAdministrationRepository;
import nursing_home.example.demo.staff.Repository.VitalsRepository;
import nursing_home.example.demo.staff.model.ActivityLog;
import nursing_home.example.demo.staff.model.IncidentReport;
import nursing_home.example.demo.staff.model.MedicationAdministration;
import nursing_home.example.demo.staff.model.Vitals;

@Service
public class AnalyticsService {
    private final ResidentRepository residentRepository;
    private final VitalsRepository vitalsRepository;
    private final ActivityLogRepository activityLogRepository;
    private final MedicationAdministrationRepository medicationRepository;
    private final IncidentReportRepository incidentRepository;
    private final double temperatureNormalMin;
    private final double temperatureNormalMax;

    public AnalyticsService(ResidentRepository residentRepository, VitalsRepository vitalsRepository,
            ActivityLogRepository activityLogRepository, MedicationAdministrationRepository medicationRepository,
            IncidentReportRepository incidentRepository,
            @Value("${vitals.temperature.normal-min:36.1}") double temperatureNormalMin,
            @Value("${vitals.temperature.normal-max:37.2}") double temperatureNormalMax) {
        this.residentRepository = residentRepository;
        this.vitalsRepository = vitalsRepository;
        this.activityLogRepository = activityLogRepository;
        this.medicationRepository = medicationRepository;
        this.incidentRepository = incidentRepository;
        this.temperatureNormalMin = temperatureNormalMin;
        this.temperatureNormalMax = temperatureNormalMax;
    }

    public DashboardResponse getDashboard(Long residentId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        if (residentRepository.findById(residentId).isEmpty()) {
            throw new ResidentNotFoundException();
        }

        List<Vitals> vitals = vitalsRepository.findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(
                residentId, startDate, endDate);
        List<ActivityLog> activities = activityLogRepository
                .findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
                        residentId, startDate, endDate);
        List<MedicationAdministration> medications = medicationRepository
                .findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(
                        residentId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        List<IncidentReport> incidents = incidentRepository.findByResidentIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                residentId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        return new DashboardResponse(startDate, endDate, analyzeVitals(vitals), analyzeActivities(activities),
                analyzeMedications(medications), analyzeIncidents(residentId, startDate, endDate, incidents),
                buildAlerts(vitals, activities, medications,
                        analyzeIncidents(residentId, startDate, endDate, incidents)));
    }

    private DashboardResponse.VitalAnalytics analyzeVitals(List<Vitals> vitals) {
        List<Vitals> ordered = vitals.stream().filter(v -> v.getDateRecorded() != null)
                .sorted(java.util.Comparator.comparing(Vitals::getDateRecorded))
                .toList();
        if (ordered.isEmpty()) {
            return new DashboardResponse.VitalAnalytics(null, null, null, null, null, "STABLE", 0, List.of());
        }
        List<Double> temperatures = ordered.stream().map(Vitals::getTemperature).toList();
        double first = temperatures.get(0);
        double latest = temperatures.get(temperatures.size() - 1);
        double change = latest - first;
        return new DashboardResponse.VitalAnalytics(latest, average(temperatures),
                temperatures.stream().min(Double::compare).orElse(0D),
                temperatures.stream().max(Double::compare).orElse(0D), change, trend(change), ordered.size(),
                ordered.stream()
                        .map(v -> new DashboardResponse.TemperaturePoint(v.getDateRecorded(), v.getTemperature()))
                        .toList());
    }

    private DashboardResponse.ActivityAnalytics analyzeActivities(List<ActivityLog> activities) {
        int scheduled = activities.size();
        int completed = activities.size();
        return new DashboardResponse.ActivityAnalytics(scheduled, completed, scheduled - completed,
                percentage(completed, scheduled), List.of());
    }

    private DashboardResponse.MedicationAnalytics analyzeMedications(List<MedicationAdministration> medications) {
        int scheduled = medications.size();
        return new DashboardResponse.MedicationAnalytics(scheduled, scheduled, 0, percentage(scheduled, scheduled));
    }

    private DashboardResponse.IncidentAnalytics analyzeIncidents(Long residentId, LocalDate startDate,
            LocalDate endDate,
            List<IncidentReport> incidents) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        int todayCount = countIncidents(residentId, today, today);
        int weekCount = countIncidents(residentId, weekStart, today);
        int monthCount = countIncidents(residentId, monthStart, today);
        long periodLength = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate previousEnd = startDate.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(periodLength - 1);
        int previous = incidentRepository.findByResidentIdAndCreatedAtBetweenOrderByCreatedAtDesc(residentId,
                previousStart.atStartOfDay(), previousEnd.atTime(LocalTime.MAX)).size();
        double change = previous == 0 ? (incidents.size() == 0 ? 0 : 100)
                : ((incidents.size() - previous) * 100.0 / previous);
        Map<String, Integer> byType = incidents.stream().collect(Collectors.toMap(i -> label(i.getTitle()), i -> 1,
                Integer::sum, LinkedHashMap::new));
        Map<String, Integer> bySeverity = incidents.stream()
                .collect(Collectors.toMap(i -> label(i.getSeverity()), i -> 1,
                        Integer::sum, LinkedHashMap::new));
        Map<LocalDate, Long> byDate = incidents.stream().filter(i -> i.getCreatedAt() != null)
                .collect(Collectors.groupingBy(i -> i.getCreatedAt().toLocalDate(), LinkedHashMap::new,
                        Collectors.counting()));
        List<DashboardResponse.IncidentPoint> chart = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            chart.add(new DashboardResponse.IncidentPoint(date, byDate.getOrDefault(date, 0L).intValue()));
        }
        return new DashboardResponse.IncidentAnalytics(todayCount, weekCount, monthCount, previous, change,
                trend(incidents.size() - previous), byType, bySeverity, chart);
    }

    private int countIncidents(Long residentId, LocalDate start, LocalDate end) {
        return incidentRepository.findByResidentIdAndCreatedAtBetweenOrderByCreatedAtDesc(residentId,
                start.atStartOfDay(), end.atTime(LocalTime.MAX)).size();
    }

    private List<DashboardResponse.Alert> buildAlerts(List<Vitals> vitals, List<ActivityLog> activities,
            List<MedicationAdministration> medications, DashboardResponse.IncidentAnalytics incidents) {
        List<DashboardResponse.Alert> alerts = new ArrayList<>();
        vitals.stream()
                .filter(v -> v.getTemperature() < temperatureNormalMin || v.getTemperature() > temperatureNormalMax)
                .findFirst().ifPresent(v -> alerts.add(new DashboardResponse.Alert("TEMPERATURE",
                        "Temperature reading is outside the configured facility range.", "WARNING", null)));
        if (activities.isEmpty())
            alerts.add(new DashboardResponse.Alert("ACTIVITY",
                    "Activity completion has not been recorded for this period.", "INFO", null));
        if (medications.isEmpty())
            alerts.add(new DashboardResponse.Alert("MEDICATION",
                    "Medication administration has not been recorded for this period.", "INFO", null));
        if (incidents.percentageChange() > 0)
            alerts.add(new DashboardResponse.Alert("INCIDENT",
                    "Incident frequency increased compared with the previous period.", "WARNING", null));
        return alerts;
    }

    private static double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private static double percentage(int numerator, int denominator) {
        return denominator == 0 ? 0 : numerator * 100.0 / denominator;
    }

    private static String trend(double change) {
        return Math.abs(change) < 0.1 ? "STABLE" : change > 0 ? "INCREASING" : "DECREASING";
    }

    private static String label(String value) {
        return value == null || value.isBlank() ? "Unspecified" : value;
    }

    public static class ResidentNotFoundException extends RuntimeException {
    }
}