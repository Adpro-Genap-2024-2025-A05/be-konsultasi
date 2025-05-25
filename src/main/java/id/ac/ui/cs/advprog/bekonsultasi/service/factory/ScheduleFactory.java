package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

import java.util.UUID;

public interface ScheduleFactory {
    Schedule createSchedule(CreateScheduleDto dto, UUID caregiverId);
    Schedule createOneTimeSchedule(CreateOneTimeScheduleDto dto, UUID caregiverId);
}

