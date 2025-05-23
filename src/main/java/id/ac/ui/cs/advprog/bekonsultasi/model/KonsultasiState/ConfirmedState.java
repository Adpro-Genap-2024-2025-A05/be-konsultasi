package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public class ConfirmedState implements KonsultasiState {

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }

    @Override
    public void confirm(Konsultasi konsultasi) {
        throw new IllegalStateException("Consultation is already confirmed");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        if (LocalDateTime.now().plusHours(24).isBefore(konsultasi.getScheduleDateTime())) {
            konsultasi.setState(new CancelledState());
        } else {
            throw new IllegalStateException("Cannot cancel a consultation less than 24 hours before the scheduled time");
        }
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        konsultasi.setState(new DoneState());
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime) {
        konsultasi.setScheduleDateTime(newDateTime);
    }
}