package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import java.time.LocalDateTime;

public class CancelledState implements KonsultasiState {
    @Override
    public void confirm(Konsultasi konsultasi) {
        throw new IllegalStateException("Konsultasi yang dibatalkan tidak dapat dikonfirmasi");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        throw new IllegalStateException("Konsultasi sudah dibatalkan");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Konsultasi yang dibatalkan tidak dapat diselesaikan");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newSchedule) {
        throw new IllegalStateException("Konsultasi yang dibatalkan tidak dapat dijadwalkan ulang");
    }

    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
