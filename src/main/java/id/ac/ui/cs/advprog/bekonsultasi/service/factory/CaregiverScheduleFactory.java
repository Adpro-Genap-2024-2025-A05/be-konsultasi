package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CaregiverScheduleFactory implements ScheduleFactory {
    @Override
    public Schedule createSchedule(CreateScheduleDto dto, UUID caregiverId) {
        return Schedule.builder()
                .caregiverId(caregiverId)
                .day(dto.getDay())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .oneTime(false)
                .build();
    }

    @Override
    public Schedule createOneTimeSchedule(CreateOneTimeScheduleDto dto, UUID caregiverId) {
        return Schedule.builder()
                .caregiverId(caregiverId)
                .day(dto.getSpecificDate().getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .specificDate(dto.getSpecificDate())
                .oneTime(true)
                .build();
    }
}