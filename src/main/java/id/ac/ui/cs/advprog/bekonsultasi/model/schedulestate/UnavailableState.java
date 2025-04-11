package id.ac.ui.cs.advprog.bekonsultasi.model.schedulestate;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class UnavailableState implements ScheduleState {
    @Override
    public String getStatus() {
        return "Unavailable";
    }

    @Override
    public boolean canBeBooked() {
        return false;
    }

    @Override
    public void book(Schedule schedule) {
        ScheduleState.StateHelper.throwCannotBookUnavailable(schedule);
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        ScheduleState.StateHelper.transitionToAvailable(schedule);
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        // Already unavailable, no change
    }
}