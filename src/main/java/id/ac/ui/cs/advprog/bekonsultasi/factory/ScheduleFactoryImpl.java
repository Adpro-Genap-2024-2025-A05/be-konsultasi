package id.ac.ui.cs.advprog.bekonsultasi.factory;

import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.UnavailableState;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduleFactoryImpl implements ScheduleFactory {

    @Override
    public Schedule createAvailableSchedule(UUID caregiverId, String day, String time) {
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(day)
                .time(time)
                .status("Available")
                .build();

        schedule.setState(new AvailableState());
        return schedule;
    }

    @Override
    public Schedule createUnavailableSchedule(UUID caregiverId, String day, String time) {
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(day)
                .time(time)
                .status("Unavailable")
                .build();

        schedule.setState(new UnavailableState());
        return schedule;
    }
}