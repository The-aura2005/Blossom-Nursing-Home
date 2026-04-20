package nursing_home.example.demo.model.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.AssignedTaskRepository;
import nursing_home.example.demo.dao.NursingHomeUserRepository;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.model.AssignedTask;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;
import nursing_home.example.demo.model.Resident;

@Service
public class AssignedTaskService {

    private final AssignedTaskRepository assignedTaskRepository;
    private final NursingHomeUserRepository nursingHomeUserRepository;
    private final ResidentRepository residentRepository;

    public AssignedTaskService(
            AssignedTaskRepository assignedTaskRepository,
            NursingHomeUserRepository nursingHomeUserRepository,
            ResidentRepository residentRepository) {
        this.assignedTaskRepository = assignedTaskRepository;
        this.nursingHomeUserRepository = nursingHomeUserRepository;
        this.residentRepository = residentRepository;
    }

    public AssignedTask assignTask(AssignedTask task, String assignedBy) {
        if (task.getResidentId() == null) {
            throw new IllegalStateException("Resident must be selected");
        }

        Resident resident = residentRepository.findById(task.getResidentId())
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        task.setId(null);
        task.setAssignedBy(assignedBy);
        task.setStatus("PENDING");
        task.setCompletedAt(null);
        task.setCreatedAt(LocalDateTime.now());
        task.setResidentName(resident.getName());
        task.setRoomNumber(String.valueOf(resident.getRoomNumber()));
        return assignedTaskRepository.save(task);
    }

    public List<AssignedTask> getAllTasks() {
        return assignedTaskRepository.findAll();
    }

    public List<AssignedTask> getTasksForStaff(String username) {
        return assignedTaskRepository.findByAssignedToUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::enrichTaskWithCurrentResident)
                .flatMap(Optional::stream)
                .toList();
    }

    public long getPendingCountForStaff(String username) {
        return getTasksForStaff(username).stream()
                .filter(task -> "PENDING".equalsIgnoreCase(task.getStatus()))
                .count();
    }

    public long getCompletedCountForStaff(String username) {
        return getTasksForStaff(username).stream()
                .filter(task -> "COMPLETED".equalsIgnoreCase(task.getStatus()))
                .count();
    }

    public void completeTask(Long taskId, String username) {
        Optional<AssignedTask> taskOptional = assignedTaskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalStateException("Task not found");
        }

        AssignedTask task = taskOptional.get();
        if (!username.equals(task.getAssignedToUsername())) {
            throw new IllegalStateException("You can only complete your own tasks");
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        assignedTaskRepository.save(task);
    }

    public List<String> getStaffUsernames() {
        List<NursingHomeUser> staffUsers = nursingHomeUserRepository
                .findByNursingHomeUserRole(NursingHomeUserRole.STAFF);
        return staffUsers.stream().map(NursingHomeUser::getUsername).toList();
    }

    public List<ResidentTaskSummary> getAssignedResidentsForStaff(String username) {
        List<AssignedTask> tasks = getTasksForStaff(username);
        Map<String, ResidentTaskSummaryAccumulator> residentMap = new LinkedHashMap<>();

        for (AssignedTask task : tasks) {
            String key = buildResidentKey(task);
            ResidentTaskSummaryAccumulator acc = residentMap.computeIfAbsent(key, k -> {
                String residentStatus = resolveResidentStatus(task.getResidentId());
                return new ResidentTaskSummaryAccumulator(
                        task.getResidentId(),
                        task.getResidentName(),
                        task.getRoomNumber(),
                        residentStatus);
            });

            acc.taskCount++;
            if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
                acc.completedCount++;
            } else {
                acc.pendingCount++;
            }
        }

        return residentMap.values().stream()
                .map(acc -> new ResidentTaskSummary(
                        acc.residentId,
                        acc.residentName,
                        acc.roomNumber,
                        acc.residentStatus,
                        acc.taskCount,
                        acc.pendingCount,
                        acc.completedCount))
                .toList();
    }

    public boolean isResidentAssignedToStaff(String username, Long residentId) {
        if (residentId == null) {
            return false;
        }

        Set<Long> assignedResidentIds = getAssignedResidentsForStaff(username).stream()
                .map(ResidentTaskSummary::residentId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        return assignedResidentIds.contains(residentId);
    }

    public Optional<ResidentTaskSummary> getAssignedResidentForStaff(String username, Long residentId) {
        if (residentId == null) {
            return Optional.empty();
        }

        return getAssignedResidentsForStaff(username).stream()
                .filter(summary -> residentId.equals(summary.residentId()))
                .findFirst();
    }

    public List<Resident> getAllResidentsForAssignment() {
        return residentRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    private String buildResidentKey(AssignedTask task) {
        if (task.getResidentId() != null) {
            return "ID:" + task.getResidentId();
        }
        return (task.getResidentName() + "|" + task.getRoomNumber()).toLowerCase();
    }

    private String resolveResidentStatus(Long residentId) {
        if (residentId == null) {
            return "Assigned";
        }
        return residentRepository.findById(residentId)
                .map(Resident::getStatus)
                .orElse("Assigned");
    }

    private Optional<AssignedTask> enrichTaskWithCurrentResident(AssignedTask task) {
        if (task.getResidentId() == null) {
            return Optional.of(task);
        }

        Optional<Resident> residentOptional = residentRepository.findById(task.getResidentId());
        if (residentOptional.isEmpty()) {
            // Skip stale tasks that point to residents that no longer exist.
            return Optional.empty();
        }

        Resident resident = residentOptional.get();
        task.setResidentName(resident.getName());
        task.setRoomNumber(String.valueOf(resident.getRoomNumber()));
        return Optional.of(task);
    }

    public record ResidentTaskSummary(
            Long residentId,
            String residentName,
            String roomNumber,
            String residentStatus,
            long taskCount,
            long pendingCount,
            long completedCount) {
    }

    private static class ResidentTaskSummaryAccumulator {
        private final Long residentId;
        private final String residentName;
        private final String roomNumber;
        private final String residentStatus;
        private long taskCount;
        private long pendingCount;
        private long completedCount;

        private ResidentTaskSummaryAccumulator(Long residentId, String residentName, String roomNumber,
                String residentStatus) {
            this.residentId = residentId;
            this.residentName = residentName;
            this.roomNumber = roomNumber;
            this.residentStatus = residentStatus;
        }
    }
}
