package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public class BookedState implements ScheduleState {
    @Override
    public String getStatus() {
        return "Booked";
    }

    @Override
    public boolean canBeBooked() {
        return false;
    }

    @Override
    public void book(Schedule schedule) {
        ScheduleState.StateHelper.throwAlreadyBooked(schedule);
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        ScheduleState.StateHelper.transitionToAvailable(schedule);
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        ScheduleState.StateHelper.transitionToUnavailable(schedule);
    }
}