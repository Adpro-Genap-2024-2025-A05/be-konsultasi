package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

import java.util.UUID;

public class RejectedState implements ScheduleState {
    @Override
    public String getStatus() {
        return "REJECTED";
    }

    @Override
    public void approve(Schedule schedule) {
        throw new IllegalStateException("Cannot approve a rejected schedule");
    }

    @Override
    public void reject(Schedule schedule) {
        throw new IllegalStateException("Schedule is already rejected");
    }

    @Override
    public void request(Schedule schedule, UUID patientId) {
        throw new IllegalStateException("Cannot request a rejected schedule");
    }
}