package id.ac.ui.cs.advprog.bekonsultasi.model;

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
        throw new IllegalStateException("Schedule is already booked");
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        schedule.setState(new AvailableState());
        schedule.setStatus("Available");
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        schedule.setState(new UnavailableState());
        schedule.setStatus("Unavailable");
    }
}