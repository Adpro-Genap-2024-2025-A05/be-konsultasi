package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public class DoneState implements KonsultasiState {

    @Override
    public String getStateName() {
        return "DONE";
    }

    @Override
    public void confirm(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot confirm a completed consultation");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot cancel a completed consultation");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Consultation is already completed");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime) {
        throw new IllegalStateException("Cannot reschedule a completed consultation");
    }
}