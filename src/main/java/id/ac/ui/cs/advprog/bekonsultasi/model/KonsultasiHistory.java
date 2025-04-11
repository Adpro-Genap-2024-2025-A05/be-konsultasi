package id.ac.ui.cs.advprog.bekonsultasi.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class KonsultasiHistory {
    private String id;
    private Konsultasi konsultasi;
    private LocalDateTime timestamp;
    private String description;

    public KonsultasiHistory(Konsultasi konsultasi, String description) {
        this.konsultasi = konsultasi;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
}