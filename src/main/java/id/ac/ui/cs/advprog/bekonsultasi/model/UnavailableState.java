package id.ac.ui.cs.advprog.bekonsultasi.model;

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
        throw new IllegalStateException("Cannot book unavailable schedule");
    }

    @Override
    public void makeAvailable(Schedule schedule) {
        schedule.setState(new AvailableState());
        schedule.setStatus("Available");
    }

    @Override
    public void makeUnavailable(Schedule schedule) {
        // Already unavailable, no change
    }
}