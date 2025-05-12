package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class UnavailableState implements ScheduleState {
    @Override
    public String getStatus() {
        return "UNAVAILABLE";
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        schedule.setState(new AvailableState());
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
    }
}