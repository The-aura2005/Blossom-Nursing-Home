package nursing_home.example.demo.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        LocalDate startDate,
        LocalDate endDate,
        VitalAnalytics vital,
        ActivityAnalytics activity,
        MedicationAnalytics medication,
        IncidentAnalytics incident,
        List<Alert> alerts) {

    public record VitalAnalytics(
            Double latestTemperature,
            Double averageTemperature,
            Double minimumTemperature,
            Double maximumTemperature,
            Double temperatureChange,
            String trend,
            int readingCount,
            List<TemperaturePoint> chartData) {
    }

    public record TemperaturePoint(LocalDate date, Double temperature) {
    }

    public record ActivityAnalytics(
            int scheduledActivities,
            int completedActivities,
            int incompleteActivities,
            double completionPercentage,
            List<String> unrecordedActivities) {
    }

    public record MedicationAnalytics(
            int scheduledMedications,
            int recordedAdministration,
            int administrationNotRecorded,
            double administrationPercentage) {
    }

    public record IncidentAnalytics(
            int incidentsToday,
            int incidentsThisWeek,
            int incidentsThisMonth,
            int previousComparablePeriod,
            double percentageChange,
            String trend,
            Map<String, Integer> byType,
            Map<String, Integer> bySeverity,
            List<IncidentPoint> chartData) {
    }

    public record IncidentPoint(LocalDate date, int count) {
    }

    public record Alert(String type, String message, String severity, LocalDateTime createdAt) {
    }
}