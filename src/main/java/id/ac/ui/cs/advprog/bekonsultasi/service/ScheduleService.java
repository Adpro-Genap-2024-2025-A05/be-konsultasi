package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

import java.util.List;
import java.util.UUID;

public interface ScheduleService {
    Schedule createSchedule(UUID caregiverId, String day, String time);
    List<Schedule> getAvailableSchedules(UUID caregiverId);
    Schedule getScheduleById(UUID id);
    void updateScheduleStatus(UUID id, String status);
}