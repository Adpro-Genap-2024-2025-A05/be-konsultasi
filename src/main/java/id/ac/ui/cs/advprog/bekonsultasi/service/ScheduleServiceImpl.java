package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.UnavailableState;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import id.ac.ui.cs.advprog.bekonsultasi.service.factory.ScheduleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final KonsultasiRepository konsultasiRepository;
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

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(UUID scheduleId, CreateScheduleDto dto, UUID caregiverId) {
        Schedule schedule = findScheduleById(scheduleId);

        // Verify ownership
        if (!schedule.getCaregiverId().equals(caregiverId)) {
            throw new AuthenticationException("You can only update your own schedules");
        }

        // Check if schedule is currently in use
        if ("UNAVAILABLE".equals(schedule.getStatus())) {
            List<Konsultasi> activeKonsultations = konsultasiRepository.findByScheduleId(scheduleId);
            if (!activeKonsultations.isEmpty()) {
                throw new ScheduleException("Cannot update schedule that is currently used in active consultations");
            }
        }

        validateScheduleTimes(dto);

        // Check for conflicts with other schedules (excluding the current one being updated)
        List<Schedule> otherCaregiverSchedules = scheduleRepository.findByCaregiverId(caregiverId).stream()
                .filter(s -> !s.getId().equals(scheduleId))
                .collect(Collectors.toList());

        validateNoOverlappingSchedules(otherCaregiverSchedules, dto, caregiverId);

        // Update schedule properties
        schedule.setDay(dto.getDay());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID caregiverId) {
        Schedule schedule = findScheduleById(scheduleId);

        // Verify ownership
        if (!schedule.getCaregiverId().equals(caregiverId)) {
            throw new AuthenticationException("You can only delete your own schedules");
        }

        // Check for active konsultasi linked to this schedule
        List<Konsultasi> linkedKonsultations = konsultasiRepository.findByScheduleId(scheduleId);

        boolean hasActiveKonsultasi = linkedKonsultations.stream()
                .anyMatch(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"));

        if (hasActiveKonsultasi) {
            throw new ScheduleException("Cannot delete schedule with active consultations");
        }

        scheduleRepository.deleteById(scheduleId);
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

    @Override
    public List<ScheduleResponseDto> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        initializeScheduleStates(schedules);
        return convertToResponseDtoList(schedules);
    }

    private void initializeScheduleStates(List<Schedule> schedules) {
        schedules.forEach(this::initializeScheduleState);
    }

    private void initializeScheduleState(Schedule schedule) {
        switch (schedule.getStatus()) {
            case "AVAILABLE" -> schedule.setState(new AvailableState());
            case "UNAVAILABLE" -> schedule.setState(new UnavailableState());
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
        initializeScheduleState(schedule);

        if ("AVAILABLE".equals(status)) {
            schedule.makeAvailable();
        } else if ("UNAVAILABLE".equals(status)) {
            schedule.makeUnavailable();
        } else {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        scheduleRepository.save(schedule);
    }

    private Schedule findScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));
    }
}