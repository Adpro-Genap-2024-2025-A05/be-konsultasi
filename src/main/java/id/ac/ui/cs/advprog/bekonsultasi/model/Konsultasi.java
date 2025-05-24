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

    @Column
    private LocalDateTime originalScheduleDateTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Transient
    private KonsultasiState state;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastUpdated == null) {
            lastUpdated = now;
        }
        if (status == null) {
            status = "REQUESTED";
            state = new RequestedState();
        }
    }

    @PreUpdate
    public void onUpdate() {
        lastUpdated = LocalDateTime.now();
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

    public LocalDateTime getOriginalScheduleDateTime() {
        return originalScheduleDateTime;
    }

    public void setOriginalScheduleDateTime(LocalDateTime originalScheduleDateTime) {
        this.originalScheduleDateTime = originalScheduleDateTime;
    }
}