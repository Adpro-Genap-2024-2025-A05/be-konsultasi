package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CaregiverScheduleFactory implements ScheduleFactory {
    @Override
    public Schedule createSchedule(CreateScheduleDto dto, UUID caregiverId) {
        Schedule schedule = Schedule.builder()
                .caregiverId(caregiverId)
                .day(dto.getDay())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status("AVAILABLE")
                .build();

        schedule.setState(new AvailableState());
        return schedule;
    }
}