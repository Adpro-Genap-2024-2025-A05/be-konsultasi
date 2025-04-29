package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

public interface ScheduleState {
    String getStatus();
    boolean canBeBooked();
    void book(Schedule schedule);
    void makeAvailable(Schedule schedule);
    void makeUnavailable(Schedule schedule);

    // Helper class for shared logic
    class StateHelper {
        public static void transitionToAvailable(Schedule schedule) {
            schedule.setState(new AvailableState());
            schedule.setStatus("Available");
        }

        public static void transitionToUnavailable(Schedule schedule) {
            schedule.setState(new UnavailableState());
            schedule.setStatus("Unavailable");
        }

        public static void throwAlreadyBooked(Schedule schedule) {
            throw new IllegalStateException("Schedule is already booked");
        }

        public static void throwCannotBookUnavailable(Schedule schedule) {
            throw new IllegalStateException("Cannot book unavailable schedule");
        }
    }
}