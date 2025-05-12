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
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleFactory scheduleFactory;

    @Override
    public ScheduleResponseDto createSchedule(CreateScheduleDto dto, UUID caregiverId) {
        validateScheduleTimes(dto);

        List<Schedule> caregiverSchedules = scheduleRepository.findByCaregiverId(caregiverId);
        validateNoOverlappingSchedules(caregiverSchedules, dto, caregiverId);

        Schedule schedule = scheduleFactory.createSchedule(dto, caregiverId);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    private void validateScheduleTimes(CreateScheduleDto dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    private void validateNoOverlappingSchedules(List<Schedule> caregiverSchedules,
                                                CreateScheduleDto newSchedule,
                                                UUID caregiverId) {
        List<Schedule> conflictingSchedules = findOverlappingSchedules(
                caregiverSchedules,
                newSchedule.getDay(),
                newSchedule.getStartTime(),
                newSchedule.getEndTime()
        );

        if (!conflictingSchedules.isEmpty()) {
            throw new ScheduleConflictException(
                    "Schedule conflicts with existing schedule(s). " +
                            "You already have a schedule on " + newSchedule.getDay() +
                            " that overlaps with the time period " +
                            newSchedule.getStartTime() + " to " + newSchedule.getEndTime()
            );
        }
    }

    private List<Schedule> findOverlappingSchedules(List<Schedule> schedules,
                                                    DayOfWeek day,
                                                    LocalTime startTime,
                                                    LocalTime endTime) {
        return schedules.stream()
                .filter(schedule -> schedule.getDay() == day)
                .filter(schedule -> isTimeOverlapping(
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        startTime,
                        endTime))
                .collect(Collectors.toList());
    }

    private boolean isTimeOverlapping(LocalTime existingStart,
                                      LocalTime existingEnd,
                                      LocalTime newStart,
                                      LocalTime newEnd) {
        return (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) ||
                newStart.equals(existingStart) ||
                newEnd.equals(existingEnd);
    }

    @Override
    public List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId) {
        List<Schedule> schedules = scheduleRepository.findByCaregiverId(caregiverId);
        initializeScheduleStates(schedules);
        return convertToResponseDtoList(schedules);
    }

    private void initializeScheduleStates(List<Schedule> schedules) {
        schedules.forEach(this::initializeScheduleState);
    }

    private void initializeScheduleState(Schedule schedule) {
        switch (schedule.getStatus()) {
            case "AVAILABLE" -> schedule.setState(new AvailableState());
            case "REQUESTED" -> schedule.setState(new RequestedState());
            case "APPROVED" -> schedule.setState(new ApprovedState());
            default -> throw new IllegalStateException("Unknown schedule status: " + schedule.getStatus());
        }
    }

    private List<ScheduleResponseDto> convertToResponseDtoList(List<Schedule> schedules) {
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
        Schedule schedule = findScheduleById(scheduleId);
        updateScheduleState(schedule, status);
        scheduleRepository.save(schedule);
    }

    private Schedule findScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));
    }

    private void updateScheduleState(Schedule schedule, String status) {
        switch (status) {
            case "REQUESTED" -> schedule.setState(new RequestedState());
            case "APPROVED" -> schedule.setState(new ApprovedState());
            case "AVAILABLE" -> schedule.setState(new AvailableState());
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}