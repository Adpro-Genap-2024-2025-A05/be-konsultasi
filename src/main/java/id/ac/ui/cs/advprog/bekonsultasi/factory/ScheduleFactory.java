package id.ac.ui.cs.advprog.bekonsultasi.factory;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

import java.util.UUID;

public interface ScheduleFactory {
    Schedule createAvailableSchedule(UUID caregiverId, String day, String time);
    Schedule createUnavailableSchedule(UUID caregiverId, String day, String time);
}