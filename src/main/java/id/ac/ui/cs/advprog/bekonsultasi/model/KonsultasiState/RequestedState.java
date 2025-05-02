package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public class RequestedState implements KonsultasiState {

    @Override
    public String getStateName() {
        return "REQUESTED";
    }

    @Override
    public void confirm(Konsultasi konsultasi) {
        konsultasi.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        konsultasi.setState(new CancelledState());
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot complete a consultation that's in requested state");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime) {
        konsultasi.setScheduleDateTime(newDateTime);
    }
}