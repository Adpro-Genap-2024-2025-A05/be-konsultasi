package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.ScheduleState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID caregiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek day;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Transient
    private ScheduleState state;

    @Column(nullable = false)
    private String status;

    public void setState(ScheduleState state) {
        this.state = state;
        this.status = state.getStatus();
    }

    public String getStatus() {
        return status;
    }

    public void approve() {
        state.approve(this);
    }

}