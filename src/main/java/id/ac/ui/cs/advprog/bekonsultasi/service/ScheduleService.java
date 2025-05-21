package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleService {
    ScheduleResponseDto createSchedule(CreateScheduleDto dto, UUID caregiverId);
    ScheduleResponseDto createOneTimeSchedule(CreateOneTimeScheduleDto dto, UUID caregiverId);
    ScheduleResponseDto updateSchedule(UUID scheduleId, CreateScheduleDto dto, UUID caregiverId);
    void deleteSchedule(UUID scheduleId, UUID caregiverId);
    List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId);
    List<ScheduleResponseDto> getAllSchedules();
    boolean isScheduleAvailableForDateTime(UUID scheduleId, LocalDateTime dateTime);
    List<LocalDateTime> getAvailableDateTimesForSchedule(UUID scheduleId, int weeksAhead);
}