package id.ac.ui.cs.advprog.bekonsultasi.model;

public class AvailableState implements ScheduleState {

    @Override
    public String getStatus() {
        return "Available";
    }

    @Override
    public boolean canBeBooked() {
        return true;
    }

    @Override
    public void book(Schedule schedule) {
        schedule.setState(new BookedState());
        schedule.setStatus("Booked");
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        // Already available, no change
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        schedule.setState(new UnavailableState());
        schedule.setStatus("Unavailable");
    }
}