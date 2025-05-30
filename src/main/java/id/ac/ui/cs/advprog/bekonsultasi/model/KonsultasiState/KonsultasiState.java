package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;

import java.time.LocalDateTime;

public interface KonsultasiState {
    String getStateName();
    void confirm(Konsultasi konsultasi);
    void cancel(Konsultasi konsultasi);
    void complete(Konsultasi konsultasi);
    void reschedule(Konsultasi konsultasi, LocalDateTime newDateTime);
}