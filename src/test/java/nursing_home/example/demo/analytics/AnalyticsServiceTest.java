package nursing_home.example.demo.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.ActivityLogRepository;
import nursing_home.example.demo.staff.Repository.IncidentReportRepository;
import nursing_home.example.demo.staff.Repository.MedicationAdministrationRepository;
import nursing_home.example.demo.staff.Repository.VitalsRepository;
import nursing_home.example.demo.staff.model.Vitals;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock
    ResidentRepository residentRepository;
    @Mock
    VitalsRepository vitalsRepository;
    @Mock
    ActivityLogRepository activityLogRepository;
    @Mock
    MedicationAdministrationRepository medicationRepository;
    @Mock
    IncidentReportRepository incidentRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(residentRepository, vitalsRepository, activityLogRepository,
                medicationRepository, incidentRepository, 36.1, 37.2);
    }

    @Test
    void calculatesTemperatureSummaryAndIncreasingTrend() {
        Long residentId = 12L;
        Resident resident = new Resident();
        resident.setId(residentId);
        when(residentRepository.findById(residentId)).thenReturn(java.util.Optional.of(resident));
        Vitals first = vital(LocalDate.of(2026, 8, 1), 36.5);
        Vitals latest = vital(LocalDate.of(2026, 8, 2), 38.1);
        when(vitalsRepository.findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(eq(residentId), any(),
                any()))
                .thenReturn(List.of(latest, first));
        when(activityLogRepository.findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
                eq(residentId), any(), any()))
                .thenReturn(List.of());
        when(medicationRepository
                .findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(eq(residentId), any(), any()))
                .thenReturn(List.of());
        when(incidentRepository.findByResidentIdAndCreatedAtBetweenOrderByCreatedAtDesc(eq(residentId), any(), any()))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(residentId, LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2));

        assertThat(response.vital().latestTemperature()).isEqualTo(38.1);
        assertThat(response.vital().averageTemperature()).isEqualTo(37.3);
        assertThat(response.vital().temperatureChange()).isCloseTo(1.6, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(response.vital().trend()).isEqualTo("INCREASING");
        assertThat(response.vital().readingCount()).isEqualTo(2);
    }

    @Test
    void returnsZeroPercentagesWhenNoRecordsExist() {
        Long residentId = 12L;
        when(residentRepository.findById(residentId)).thenReturn(java.util.Optional.of(new Resident()));
        when(vitalsRepository.findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(eq(residentId), any(),
                any())).thenReturn(List.of());
        when(activityLogRepository.findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
                eq(residentId), any(), any())).thenReturn(List.of());
        when(medicationRepository
                .findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(eq(residentId), any(), any()))
                .thenReturn(List.of());
        when(incidentRepository.findByResidentIdAndCreatedAtBetweenOrderByCreatedAtDesc(eq(residentId), any(), any()))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(residentId, LocalDate.now(), LocalDate.now());

        assertThat(response.activity().completionPercentage()).isZero();
        assertThat(response.medication().administrationPercentage()).isZero();
        assertThat(response.incident().percentageChange()).isZero();
    }

    @Test
    void rejectsReversedDateRange() {
        assertThatThrownBy(() -> service.getDashboard(12L, LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must not be after endDate");
    }

    private static Vitals vital(LocalDate date, double temperature) {
        Vitals vital = new Vitals();
        vital.setDateRecorded(date);
        vital.setTemperature(temperature);
        return vital;
    }
}