package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class AvailableState implements ScheduleState {
    @Override
    public String getStatus() {
        return "AVAILABLE";
    }

    @Override
    public void makeAvailable(Schedule schedule) {
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        schedule.setState(new UnavailableState());
    }
}