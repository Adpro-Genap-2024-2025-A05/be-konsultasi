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

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        if (!schedule.getCaregiverId().equals(caregiverId)) {
            throw new AuthenticationException("You can only update your own schedules");
        }

        List<Konsultasi> activeKonsultations = konsultasiRepository.findByScheduleId(scheduleId).stream()
                .filter(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"))
                .filter(k -> k.getScheduleDateTime().isAfter(LocalDateTime.now()))
                .toList();

        if (!activeKonsultations.isEmpty()) {
            throw new ScheduleException("Cannot update schedule that is currently used in active consultations");
        }

        validateScheduleTimes(dto);

        List<Schedule> otherCaregiverSchedules = scheduleRepository.findByCaregiverId(caregiverId).stream()
                .filter(s -> !s.getId().equals(scheduleId))
                .collect(Collectors.toList());

        validateNoOverlappingSchedules(otherCaregiverSchedules, dto, caregiverId);

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

        if (!schedule.getCaregiverId().equals(caregiverId)) {
            throw new AuthenticationException("You can only delete your own schedules");
        }

        List<Konsultasi> futureKonsultations = konsultasiRepository.findByScheduleId(scheduleId).stream()
                .filter(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"))
                .filter(k -> k.getScheduleDateTime().isAfter(LocalDateTime.now()))
                .toList();

        if (!futureKonsultations.isEmpty()) {
            throw new ScheduleException("Cannot delete schedule with future consultations");
        }

        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public List<ScheduleResponseDto> getCaregiverSchedules(UUID caregiverId) {
        List<Schedule> schedules = scheduleRepository.findByCaregiverId(caregiverId);
        return convertToResponseDtoList(schedules);
    }

    @Override
    public List<ScheduleResponseDto> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return convertToResponseDtoList(schedules);
    }

    @Override
    public boolean isScheduleAvailableForDateTime(UUID scheduleId, LocalDateTime dateTime) {
        Schedule schedule = findScheduleById(scheduleId);

        if (schedule.isOneTime()) {
            if (schedule.getSpecificDate() == null ||
                    !schedule.getSpecificDate().equals(dateTime.toLocalDate())) {
                return false;
            }
        }

        else if (schedule.getDay() != dateTime.getDayOfWeek()) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(schedule.getStartTime()) || !time.isBefore(schedule.getEndTime())) {
            return false;
        }

        List<Konsultasi> existingConsultations = konsultasiRepository.findByScheduleId(scheduleId);

        return existingConsultations.stream()
                .filter(k -> !k.getStatus().equals("CANCELLED") && !k.getStatus().equals("DONE"))
                .noneMatch(k -> {

                    if (isTimeConflict(k.getScheduleDateTime(), dateTime)) {
                        return true;
                    }

                    if ("RESCHEDULED".equals(k.getStatus()) && k.getOriginalScheduleDateTime() != null) {
                        return isTimeConflict(k.getOriginalScheduleDateTime(), dateTime);
                    }

                    return false;
                });
    }

    @Override
    public List<LocalDateTime> getAvailableDateTimesForSchedule(UUID scheduleId, int weeksAhead) {
        Schedule schedule = findScheduleById(scheduleId);
        List<LocalDateTime> availableTimes = new ArrayList<>();

        if (schedule.isOneTime()) {
            if (schedule.getSpecificDate() != null) {
                LocalDateTime dateTime = LocalDateTime.of(
                        schedule.getSpecificDate(),
                        schedule.getStartTime()
                );

                if (dateTime.isAfter(LocalDateTime.now()) &&
                        isScheduleAvailableForDateTime(scheduleId, dateTime)) {
                    availableTimes.add(dateTime);
                }
            }
            return availableTimes;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDate nextDay = today.with(TemporalAdjusters.nextOrSame(schedule.getDay()));

        if (nextDay.equals(today) && now.toLocalTime().isAfter(schedule.getStartTime())) {
            nextDay = today.with(TemporalAdjusters.next(schedule.getDay()));
        }

        for (int i = 0; i < weeksAhead; i++) {
            LocalDate dateToCheck = nextDay.plusWeeks(i);
            LocalDateTime dateTimeToCheck = LocalDateTime.of(
                    dateToCheck,
                    schedule.getStartTime()
            );

            if (isScheduleAvailableForDateTime(scheduleId, dateTimeToCheck)) {
                availableTimes.add(dateTimeToCheck);
            }
        }

        return availableTimes;
    }

    @Override
    public ScheduleResponseDto createOneTimeSchedule(CreateOneTimeScheduleDto dto, UUID caregiverId) {
        if (dto.getSpecificDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("One-time schedule date must be in the future");
        }

        validateScheduleTimes(dto.getStartTime(), dto.getEndTime());

        LocalDateTime startDateTime = LocalDateTime.of(dto.getSpecificDate(), dto.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(dto.getSpecificDate(), dto.getEndTime());

        List<Schedule> caregiverSchedules = scheduleRepository.findByCaregiverId(caregiverId);

        for (Schedule existingSchedule : caregiverSchedules) {

            if (existingSchedule.isOneTime() && existingSchedule.getSpecificDate() != null) {
                if (existingSchedule.getSpecificDate().equals(dto.getSpecificDate())) {

                    if (isTimeOverlapping(
                            existingSchedule.getStartTime(),
                            existingSchedule.getEndTime(),
                            dto.getStartTime(),
                            dto.getEndTime())) {
                        throw new ScheduleConflictException(
                                "Schedule conflicts with existing one-time schedule on " +
                                        dto.getSpecificDate());
                    }
                }
            }

            else if (existingSchedule.getDay() == dto.getSpecificDate().getDayOfWeek()) {

                if (isTimeOverlapping(
                        existingSchedule.getStartTime(),
                        existingSchedule.getEndTime(),
                        dto.getStartTime(),
                        dto.getEndTime())) {
                    throw new ScheduleConflictException(
                            "Schedule conflicts with existing recurring schedule on " +
                                    dto.getSpecificDate().getDayOfWeek());
                }
            }
        }

        Schedule schedule = scheduleFactory.createOneTimeSchedule(dto, caregiverId);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    @Override
    public List<ScheduleResponseDto> getAvailableSchedulesByCaregiver(UUID caregiverId) {
        List<Schedule> allSchedules = scheduleRepository.findByCaregiverId(caregiverId);
        
        List<Schedule> availableSchedules = allSchedules.stream()
                .filter(this::isScheduleCurrentlyAvailable)
                .collect(Collectors.toList());
        
        return convertToResponseDtoList(availableSchedules);
    }

    @Override
    public List<ScheduleResponseDto> getAvailableSchedulesForCaregivers(List<UUID> caregiverIds) {
        List<Schedule> allSchedules = scheduleRepository.findByCaregiverIdIn(caregiverIds);
        
        List<Schedule> availableSchedules = allSchedules.stream()
                .filter(this::isScheduleCurrentlyAvailable)
                .collect(Collectors.toList());
        
        return convertToResponseDtoList(availableSchedules);
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

    public Schedule findScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));
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