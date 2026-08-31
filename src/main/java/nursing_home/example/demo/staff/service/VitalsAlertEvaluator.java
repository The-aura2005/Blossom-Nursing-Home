package nursing_home.example.demo.staff.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import nursing_home.example.demo.staff.model.Vitals;

@Component
public class VitalsAlertEvaluator {
    private final double normalTemperatureMin;
    private final double normalTemperatureMax;
    private final int normalSystolicMin;
    private final int normalSystolicMax;
    private final int normalDiastolicMin;
    private final int normalDiastolicMax;
    private final int defaultWeightMin;
    private final int defaultWeightMax;
    private final Map<Long, WeightRange> residentWeightRanges;

    public VitalsAlertEvaluator(
            @Value("${vitals.temperature.normal-min:36.1}") double normalTemperatureMin,
            @Value("${vitals.temperature.normal-max:37.2}") double normalTemperatureMax,
            @Value("${vitals.blood-pressure.systolic.normal-min:90}") int normalSystolicMin,
            @Value("${vitals.blood-pressure.systolic.normal-max:120}") int normalSystolicMax,
            @Value("${vitals.blood-pressure.diastolic.normal-min:60}") int normalDiastolicMin,
            @Value("${vitals.blood-pressure.diastolic.normal-max:80}") int normalDiastolicMax,
            @Value("${vitals.weight.default-min:0}") int defaultWeightMin,
            @Value("${vitals.weight.default-max:0}") int defaultWeightMax,
            @Value("${vitals.weight.resident-ranges:}") String residentRanges) {
        this.normalTemperatureMin = normalTemperatureMin;
        this.normalTemperatureMax = normalTemperatureMax;
        this.normalSystolicMin = normalSystolicMin;
        this.normalSystolicMax = normalSystolicMax;
        this.normalDiastolicMin = normalDiastolicMin;
        this.normalDiastolicMax = normalDiastolicMax;
        this.defaultWeightMin = defaultWeightMin;
        this.defaultWeightMax = defaultWeightMax;
        this.residentWeightRanges = parseResidentRanges(residentRanges);
    }

    public void evaluate(Vitals vital) {
        List<String> messages = new ArrayList<>();
        boolean critical = false;

        if (vital.getTemperature() < normalTemperatureMin || vital.getTemperature() > normalTemperatureMax) {
            messages.add("Abnormal Temperature (" + vital.getTemperature() + " C)");
            critical = vital.getTemperature() < 35 || vital.getTemperature() > 39;
        }

        int[] bloodPressure = parseBloodPressure(vital.getBloodPressure());
        if (bloodPressure != null) {
            int systolic = bloodPressure[0];
            int diastolic = bloodPressure[1];
            if (systolic < normalSystolicMin || diastolic < normalDiastolicMin) {
                messages.add("Low Blood Pressure (" + vital.getBloodPressure() + ")");
            } else if (systolic > normalSystolicMax || diastolic > normalDiastolicMax) {
                messages.add("High Blood Pressure (" + vital.getBloodPressure() + ")");
            }
            critical |= systolic < 80 || systolic > 180 || diastolic < 50 || diastolic > 120;
        }

        WeightRange range = vital.getResident() == null ? null : residentWeightRanges.get(vital.getResident().getId());
        if (range == null && defaultWeightMax > defaultWeightMin) {
            range = new WeightRange(defaultWeightMin, defaultWeightMax);
        }
        if (range != null && (vital.getWeight() < range.minimum() || vital.getWeight() > range.maximum())) {
            messages.add("Abnormal Weight (" + vital.getWeight() + ")");
        }

        vital.setAlertMessages(messages);
        vital.setAlertStatus(messages.isEmpty() ? "NORMAL" : critical ? "CRITICAL" : "WARNING");
        vital.setAlertMessage(messages.isEmpty() ? "No abnormal values detected." : String.join("; ", messages));
    }

    private int[] parseBloodPressure(String value) {
        if (value == null || !value.contains("/")) {
            return null;
        }
        String[] parts = value.split("/");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Map<Long, WeightRange> parseResidentRanges(String value) {
        Map<Long, WeightRange> ranges = new java.util.HashMap<>();
        if (value == null || value.isBlank()) {
            return ranges;
        }
        for (String entry : value.split(",")) {
            String[] fields = entry.trim().split(":");
            if (fields.length != 3) {
                continue;
            }
            try {
                ranges.put(Long.valueOf(fields[0]),
                        new WeightRange(Integer.parseInt(fields[1]), Integer.parseInt(fields[2])));
            } catch (NumberFormatException ignored) {
                // Ignore malformed optional overrides.
            }
        }
        return ranges;
    }

    private record WeightRange(int minimum, int maximum) {
    }
}