package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public class RescheduledState implements KonsultasiState {

    @Override
    public String getStateName() {
        return "RESCHEDULED";
    }

    @Override
    public void confirm(Konsultasi konsultasi) {
        konsultasi.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot cancel a rescheduled consultation. It must be accepted or rejected.");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Cannot complete a consultation that is in rescheduled state");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime) {
        LocalDateTime originalDateTime = konsultasi.getOriginalScheduleDateTime();
        konsultasi.setOriginalScheduleDateTime(originalDateTime);
        konsultasi.setScheduleDateTime(newDateTime);
    }
    
    public void reject(Konsultasi konsultasi) {
        if (konsultasi.getOriginalScheduleDateTime() != null) {
            konsultasi.setScheduleDateTime(konsultasi.getOriginalScheduleDateTime());
        }
        konsultasi.setState(new RequestedState());
    }
}