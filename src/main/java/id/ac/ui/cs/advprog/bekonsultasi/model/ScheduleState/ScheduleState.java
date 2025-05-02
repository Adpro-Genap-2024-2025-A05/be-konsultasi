package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public interface ScheduleState {
    String getStatus();
    void approve(Schedule schedule);
    void reject(Schedule schedule);
    void request(Schedule schedule);
}