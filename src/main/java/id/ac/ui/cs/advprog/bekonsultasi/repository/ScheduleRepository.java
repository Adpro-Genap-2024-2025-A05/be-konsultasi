package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ScheduleRepository {

    private final Map<UUID, Schedule> scheduleMap = new HashMap<>();

    public Schedule save(Schedule schedule) {
        scheduleMap.put(schedule.getId(), schedule);
        return schedule;
    }

    public Optional<Schedule> findById(UUID id) {
        return Optional.ofNullable(scheduleMap.get(id));
    }

    public List<Schedule> findAll() {
        return new ArrayList<>(scheduleMap.values());
    }

    public List<Schedule> findByCaregiverId(UUID caregiverId) {
        return scheduleMap.values().stream()
                .filter(schedule -> schedule.getCaregiverId().equals(caregiverId))
                .collect(Collectors.toList());
    }

    public List<Schedule> findByCaregiverIdAndStatus(UUID caregiverId, String status) {
        return scheduleMap.values().stream()
                .filter(schedule -> schedule.getCaregiverId().equals(caregiverId)
                        && schedule.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public void deleteById(UUID id) {
        scheduleMap.remove(id);
    }
}