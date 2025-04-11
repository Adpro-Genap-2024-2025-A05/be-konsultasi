package id.ac.ui.cs.advprog.bekonsultasi.model;

public interface ScheduleState {
    String getStatus();
    boolean canBeBooked();
    void book(Schedule schedule);
    void makeAvailable(Schedule schedule);
    void makeUnavailable(Schedule schedule);
}