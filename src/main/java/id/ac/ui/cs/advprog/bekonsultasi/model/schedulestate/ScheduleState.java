package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public interface ScheduleState {
    String getStatus();
    boolean canBeBooked();
    void book(Schedule schedule);
    void makeAvailable(Schedule schedule);
    void makeUnavailable(Schedule schedule);
}