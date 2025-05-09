package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class AvailableState implements ScheduleState {
    @Override
    public String getStatus() {
        return "AVAILABLE";
    }

    @Override
    public void approve(Schedule schedule) {
        schedule.setState(new ApprovedState());
    }

    @Override
    public void request(Schedule schedule) {
        throw new UnsupportedOperationException("Request operation not supported in this version");
    }
}