package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public class CancelledState implements KonsultasiState {

    @Override
    public String getStateName() {
        return "CANCELLED";
    }

    @Override
    public void confirm(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot confirm a cancelled consultation");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        throw new IllegalStateException("Consultation is already cancelled");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot complete a cancelled consultation");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime) {
        throw new IllegalStateException("Cannot reschedule a cancelled consultation");
    }
}