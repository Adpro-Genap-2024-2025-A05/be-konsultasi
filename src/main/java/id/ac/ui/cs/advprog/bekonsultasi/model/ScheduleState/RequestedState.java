package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class RequestedState implements ScheduleState {
    @Override
    public String getStatus() {
        return "REQUESTED";
    }

    @Override
    public void approve(Schedule schedule) {
        schedule.setState(new ApprovedState());
    }

    @Override
    public void reject(Schedule schedule) {
        schedule.setState(new RejectedState());
    }

    @Override
    public void request(Schedule schedule) {
        throw new IllegalStateException("Schedule is already requested");
    }
}