package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import java.time.LocalDateTime;

public class ConfirmedState implements KonsultasiState {
    @Override
    public void confirm(Konsultasi konsultasi) {
        throw new IllegalStateException("Konsultasi sudah dikonfirmasi");
    }

    @Override
    public void cancel(Konsultasi konsultasi) {
        if (isWithin24Hours(konsultasi)) {
            throw new IllegalStateException("Tidak dapat membatalkan konsultasi kurang dari 24 jam sebelum jadwal");
        }

        konsultasi.setState(new CancelledState());
        konsultasi.addHistory("Konsultasi dibatalkan");
    }

    @Override
    public void complete(Konsultasi konsultasi) {
        konsultasi.setState(new DoneState());
        konsultasi.addHistory("Konsultasi telah selesai");
    }

    @Override
    public void reschedule(Konsultasi konsultasi, LocalDateTime newSchedule) {
        if (isWithin24Hours(konsultasi)) {
            throw new IllegalStateException("Tidak dapat mengubah jadwal konsultasi kurang dari 24 jam sebelum jadwal");
        }

        konsultasi.setSchedule(newSchedule);
        konsultasi.addHistory("Jadwal konsultasi diubah");
    }

    private boolean isWithin24Hours(Konsultasi konsultasi) {
        return konsultasi.getSchedule().minusHours(24).isBefore(LocalDateTime.now());
    }

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
