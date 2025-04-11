package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import java.time.LocalDateTime;

public class RequestedState implements KonsultasiState {
    @Override
    public void confirm(Konsultasi konsultasi) {
        konsultasi.setState(new ConfirmedState());
        konsultasi.addHistory("Konsultasi dikonfirmasi");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        konsultasi.setState(new CancelledState());
        konsultasi.addHistory("Konsultasi dibatalkan saat masih dalam status permintaan");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        throw new IllegalStateException("Tidak dapat menyelesaikan konsultasi yang belum dikonfirmasi");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newSchedule) {
        konsultasi.setSchedule(newSchedule);
        konsultasi.addHistory("Jadwal konsultasi diubah saat masih dalam status permintaan");
    }

    @Override
    public String getStateName() {
        return "REQUESTED";
    }
}
