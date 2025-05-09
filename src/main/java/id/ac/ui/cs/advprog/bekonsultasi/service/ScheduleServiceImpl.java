package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.ApprovedState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.RequestedState;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import id.ac.ui.cs.advprog.bekonsultasi.service.factory.ScheduleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleFactory scheduleFactory;

    @Override
    public ScheduleResponseDto createSchedule(CreateScheduleDto dto, UUID caregiverId) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        List<Schedule> conflictingSchedules = scheduleRepository.findOverlappingSchedules(
                caregiverId,
                dto.getDay(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (!conflictingSchedules.isEmpty()) {
            throw new ScheduleConflictException(
                    "Schedule conflicts with existing schedule(s). " +
                            "You already have a schedule on " + dto.getDay() +
                            " that overlaps with the time period " + dto.getStartTime() + " to " + dto.getEndTime()
            );
        }

        Schedule schedule = scheduleFactory.createSchedule(dto, caregiverId);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    @Override
    public List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId) {
        List<Schedule> schedules = scheduleRepository.findByCaregiverId(caregiverId);

        schedules.forEach(schedule -> {
            switch (schedule.getStatus()) {
                case "AVAILABLE" -> schedule.setState(new AvailableState());
                case "REQUESTED" -> schedule.setState(new RequestedState());
                case "APPROVED" -> schedule.setState(new ApprovedState());
                default -> throw new IllegalStateException("Unknown schedule status: " + schedule.getStatus());
            }
        });

        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ScheduleResponseDto convertToDto(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .caregiverId(schedule.getCaregiverId())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus())
                .build();
    }

    @Override
    public void updateScheduleStatus(UUID scheduleId, String status) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));

        switch (status) {
            case "REQUESTED" -> schedule.setState(new RequestedState());
            case "APPROVED" -> schedule.setState(new ApprovedState());
            case "AVAILABLE" -> schedule.setState(new AvailableState());
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        }

        scheduleRepository.save(schedule);
    }
}