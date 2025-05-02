package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.KonsultasiState;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.RequestedState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "konsultasi")
public class Konsultasi {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID scheduleId;

    @Column(nullable = false)
    private UUID caregiverId;

    @Column(nullable = false)
    private UUID pacilianId;

    @Column(nullable = false)
    private LocalDateTime scheduleDateTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Transient
    private KonsultasiState state;

    @Column(nullable = false)
    private String status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = "REQUESTED";
            state = new RequestedState();
        }
    }

    public void setState(KonsultasiState state) {
        this.state = state;
        this.status = state.getStateName();
    }

    public void confirm() {
        state.confirm(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void complete() {
        state.complete(this);
    }

    public void reschedule(LocalDateTime newDateTime) {
        state.reschedule(this, newDateTime);
    }
}