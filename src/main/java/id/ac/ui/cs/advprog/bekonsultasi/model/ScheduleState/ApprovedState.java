package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class ApprovedState implements ScheduleState {
    @Override
    public String getStatus() {
        return "APPROVED";
    }

    @Override
    public void approve(Schedule schedule) {
        throw new IllegalStateException("Schedule is already approved");
    }

    @Override
    public void request(Schedule schedule) {
        throw new IllegalStateException("Schedule is already approved");
    }
}