package id.ac.ui.cs.advprog.bekonsultasi.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
public class Schedule {
    private UUID id;
    private UUID caregiverId;
    private String day;
    private String time;

    @Setter
    private String status;

    @Setter
    private ScheduleState state;

    public void changeStatus(String status) {
        this.status = status;
        switch (status) {
            case "Available":
                this.state = new AvailableState();
                break;
            case "Booked":
                this.state = new BookedState();
                break;
            case "Unavailable":
                this.state = new UnavailableState();
                break;
        }
    }
}