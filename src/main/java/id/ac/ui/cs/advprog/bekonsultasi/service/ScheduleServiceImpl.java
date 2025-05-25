package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import id.ac.ui.cs.advprog.bekonsultasi.service.factory.ScheduleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final KonsultasiRepository konsultasiRepository;
    private final ScheduleFactory scheduleFactory;

    private final Counter scheduleCreatedCounter;
    private final Counter scheduleOneTimeCreatedCounter;
    private final Counter scheduleUpdatedCounter;
    private final Counter scheduleDeletedCounter;
    private final Counter scheduleDeleteAsyncCounter;
    private final Counter scheduleAvailabilityCheckCounter;
    private final Counter scheduleAvailableTimesRequestCounter;
    private final Counter scheduleCaregiverQueryCounter;
    private final Counter scheduleAllQueryCounter;
    private final Counter scheduleAvailableByIdCounter;
    private final Counter scheduleAvailableMultipleCounter;
    private final Counter scheduleConflictCounter;
    private final Counter scheduleValidationErrorCounter;
    private final Counter scheduleAuthorizationErrorCounter;
    private final Counter scheduleNotFoundCounter;
    private final Counter scheduleTimeValidationErrorCounter;
    private final Counter schedulePastDateErrorCounter;
    private final Counter scheduleActiveKonsultasiBlockCounter;
    private final Counter scheduleDatabaseErrorCounter;
    private final Counter scheduleGeneralErrorCounter;
    private final Counter scheduleOverlapPreventedCounter;
    private final Counter scheduleWeeklyScheduleCounter;
    private final Counter scheduleFactoryRegularCounter;
    private final Counter scheduleFactoryOneTimeCounter;
    private final Counter scheduleSuccessfulOperationsCounter;
    private final Counter scheduleFailedOperationsCounter;

    @Override
    public ScheduleResponseDto createSchedule(CreateScheduleDto dto, UUID caregiverId) {
        try {
            validateScheduleTimes(dto);

            List<Schedule> caregiverSchedules = scheduleRepository.findByCaregiverId(caregiverId);
            validateNoOverlappingSchedules(caregiverSchedules, dto, caregiverId);

            Schedule schedule = scheduleFactory.createSchedule(dto, caregiverId);
            Schedule savedSchedule = scheduleRepository.save(schedule);

            scheduleCreatedCounter.increment();
            scheduleFactoryRegularCounter.increment();
            scheduleWeeklyScheduleCounter.increment();
            scheduleSuccessfulOperationsCounter.increment();

            return convertToDto(savedSchedule);
        } catch (IllegalArgumentException e) {
            scheduleValidationErrorCounter.increment();
            scheduleTimeValidationErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (ScheduleConflictException e) {
            scheduleConflictCounter.increment();
            scheduleOverlapPreventedCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public ScheduleResponseDto createOneTimeSchedule(CreateOneTimeScheduleDto dto, UUID caregiverId) {
        try {
            if (dto.getSpecificDate().isBefore(LocalDate.now())) {
                schedulePastDateErrorCounter.increment();
                throw new IllegalArgumentException("One-time schedule date must be in the future");
            }

            validateScheduleTimes(dto.getStartTime(), dto.getEndTime());

            Schedule schedule = scheduleFactory.createOneTimeSchedule(dto, caregiverId);
            Schedule savedSchedule = scheduleRepository.save(schedule);

            scheduleOneTimeCreatedCounter.increment();
            scheduleFactoryOneTimeCounter.increment();
            scheduleSuccessfulOperationsCounter.increment();

            return convertToDto(savedSchedule);
        } catch (IllegalArgumentException e) {
            scheduleValidationErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (ScheduleConflictException e) {
            scheduleConflictCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(UUID scheduleId, CreateScheduleDto dto, UUID caregiverId) {
        try {
            Schedule schedule = findScheduleById(scheduleId);

            if (!schedule.getCaregiverId().equals(caregiverId)) {
                scheduleAuthorizationErrorCounter.increment();
                throw new AuthenticationException("You can only update your own schedules");
            }

            List<Konsultasi> activeKonsultations = konsultasiRepository.findByScheduleId(scheduleId).stream()
                    .filter(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"))
                    .filter(k -> k.getScheduleDateTime().isAfter(LocalDateTime.now()))
                    .toList();

            if (!activeKonsultations.isEmpty()) {
                scheduleActiveKonsultasiBlockCounter.increment();
                throw new ScheduleException("Cannot update schedule that is currently used in active consultations");
            }

            validateScheduleTimes(dto);

            schedule.setDay(dto.getDay());
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());

            Schedule updatedSchedule = scheduleRepository.save(schedule);

            scheduleUpdatedCounter.increment();
            scheduleSuccessfulOperationsCounter.increment();

            return convertToDto(updatedSchedule);
        } catch (AuthenticationException e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (IllegalArgumentException e) {
            scheduleValidationErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (ScheduleException e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID caregiverId) {
        try {
            Schedule schedule = findScheduleById(scheduleId);

            if (!schedule.getCaregiverId().equals(caregiverId)) {
                scheduleAuthorizationErrorCounter.increment();
                throw new AuthenticationException("You can only delete your own schedules");
            }

            List<Konsultasi> futureKonsultations = konsultasiRepository.findByScheduleId(scheduleId).stream()
                    .filter(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"))
                    .filter(k -> k.getScheduleDateTime().isAfter(LocalDateTime.now()))
                    .toList();

            if (!futureKonsultations.isEmpty()) {
                scheduleActiveKonsultasiBlockCounter.increment();
                throw new ScheduleException("Cannot delete schedule with future consultations");
            }

            scheduleRepository.deleteById(scheduleId);

            scheduleDeletedCounter.increment();
            scheduleSuccessfulOperationsCounter.increment();
        } catch (AuthenticationException e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (ScheduleException e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> deleteScheduleAsync(UUID scheduleId, UUID caregiverId) {
        scheduleDeleteAsyncCounter.increment();

        try {
            deleteSchedule(scheduleId, caregiverId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId) {
        scheduleCaregiverQueryCounter.increment();

        try {
            List<Schedule> schedules = scheduleRepository.findByCaregiverId(caregiverId);
            scheduleSuccessfulOperationsCounter.increment();
            return convertToResponseDtoList(schedules);
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public List<ScheduleResponseDto> getAllSchedules() {
        scheduleAllQueryCounter.increment();

        try {
            List<Schedule> schedules = scheduleRepository.findAll();
            scheduleSuccessfulOperationsCounter.increment();
            return convertToResponseDtoList(schedules);
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public boolean isScheduleAvailableForDateTime(UUID scheduleId, LocalDateTime dateTime) {
        scheduleAvailabilityCheckCounter.increment();

        try {
            Schedule schedule = findScheduleById(scheduleId);

            scheduleSuccessfulOperationsCounter.increment();
            return /* your availability check result */true;
        } catch (Exception e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public List<LocalDateTime> getAvailableDateTimesForSchedule(UUID scheduleId, int weeksAhead) {
        scheduleAvailableTimesRequestCounter.increment();

        try {
            List<LocalDateTime> availableTimes = new ArrayList<>();

            scheduleSuccessfulOperationsCounter.increment();
            return availableTimes;
        } catch (Exception e) {
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public List<ScheduleResponseDto> getAvailableSchedulesByCaregiver(UUID caregiverId) {
        scheduleAvailableByIdCounter.increment();

        try {
            List<Schedule> allSchedules = scheduleRepository.findByCaregiverId(caregiverId);

            List<Schedule> availableSchedules = allSchedules.stream()
                    .filter(this::isScheduleCurrentlyAvailable)
                    .collect(Collectors.toList());

            scheduleSuccessfulOperationsCounter.increment();
            return convertToResponseDtoList(availableSchedules);
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    @Override
    public List<ScheduleResponseDto> getAvailableSchedulesForCaregivers(List<UUID> caregiverIds) {
        scheduleAvailableMultipleCounter.increment();

        try {
            List<Schedule> allSchedules = scheduleRepository.findByCaregiverIdIn(caregiverIds);

            List<Schedule> availableSchedules = allSchedules.stream()
                    .filter(this::isScheduleCurrentlyAvailable)
                    .collect(Collectors.toList());

            scheduleSuccessfulOperationsCounter.increment();
            return convertToResponseDtoList(availableSchedules);
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            scheduleGeneralErrorCounter.increment();
            scheduleFailedOperationsCounter.increment();
            throw e;
        }
    }

    public Schedule findScheduleById(UUID scheduleId) {
        try {
            return scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> {
                        scheduleNotFoundCounter.increment();
                        return new IllegalArgumentException("Schedule not found with id: " + scheduleId);
                    });
        } catch (Exception e) {
            scheduleDatabaseErrorCounter.increment();
            throw e;
        }
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

    private boolean isTimeConflict(LocalDateTime time1, LocalDateTime time2) {
        LocalDateTime end1 = time1.plusHours(1);
        LocalDateTime end2 = time2.plusHours(1);

        return time1.isBefore(end2) && time2.isBefore(end1);
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
                .specificDate(schedule.getSpecificDate())
                .oneTime(schedule.isOneTime())
                .build();
    }

    private void validateScheduleTimes(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    private boolean isScheduleCurrentlyAvailable(Schedule schedule) {
        if (schedule.isOneTime()) {
            return schedule.getSpecificDate() != null &&
                schedule.getSpecificDate().isAfter(LocalDate.now());
        } else {
            return true;
        }
    }
}