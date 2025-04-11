package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.KonsultasiState;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.RequestedState;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Konsultasi {
    private String id;
    private LocalDateTime schedule;
    private String paciliansId;
    private String careGiverId;
    private String notes;
    private String stateValue;
    private List<KonsultasiHistory> historyList = new ArrayList<>();
    private transient KonsultasiState state;

    public Konsultasi(String paciliansId, String careGiverId, LocalDateTime schedule, String notes) {
        this.paciliansId = paciliansId;
        this.careGiverId = careGiverId;
        this.schedule = schedule;
        this.notes = notes;

        this.state = new RequestedState();
        this.stateValue = state.getStateName();
    }

    public void setState(KonsultasiState state) {
        this.state = state;
        this.stateValue = state.getStateName();
    }

    public void addHistory(String description) {
        KonsultasiHistory history = new KonsultasiHistory(this, description);
        historyList.add(history);
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

    public void reschedule(LocalDateTime newSchedule) {
        state.reschedule(this, newSchedule);
    }
}
