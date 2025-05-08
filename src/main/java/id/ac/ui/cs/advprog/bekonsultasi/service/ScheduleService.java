package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;

import java.util.List;
import java.util.UUID;

public interface ScheduleService {
    ScheduleResponseDto createSchedule(CreateScheduleDto dto, UUID caregiverId);
    List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId);
    void updateScheduleStatus(UUID scheduleId, String status);
}