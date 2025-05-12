package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.UnavailableState;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import id.ac.ui.cs.advprog.bekonsultasi.service.factory.ScheduleFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleFactory scheduleFactory;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private UUID caregiverId;
    private Schedule schedule;
    private CreateScheduleDto createScheduleDto;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();

        createScheduleDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
        schedule.setState(new AvailableState());
    }

    @Test
    void testCreateSchedule() {
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(List.of());
        when(scheduleFactory.createSchedule(createScheduleDto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        assertEquals(schedule.getId(), result.getId());
        assertEquals("AVAILABLE", result.getStatus());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void testCreateScheduleWithInvalidTimes() {
        CreateScheduleDto invalidDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createSchedule(invalidDto, caregiverId)
        );
    }

    @Test
    void testCreateScheduleWithConflict() {
        Schedule existingSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(List.of(existingSchedule));

        assertThrows(ScheduleConflictException.class, () ->
                scheduleService.createSchedule(createScheduleDto, caregiverId)
        );
    }

    @Test
    void testGetCaregiverSchedules() {
        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status("UNAVAILABLE")
                .build();

        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Arrays.asList(schedule, schedule2));

        List<ScheduleResponseDto> result = scheduleService.getCaregiverSchedules(caregiverId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateScheduleStatusToAvailable() {
        UUID scheduleId = UUID.randomUUID();
        Schedule unavailableSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("UNAVAILABLE")
                .build();
        unavailableSchedule.setState(new UnavailableState());

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(unavailableSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(unavailableSchedule);

        scheduleService.updateScheduleStatus(scheduleId, "AVAILABLE");

        verify(scheduleRepository).save(unavailableSchedule);
    }

    @Test
    void testUpdateScheduleStatusToUnavailable() {
        UUID scheduleId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        scheduleService.updateScheduleStatus(scheduleId, "UNAVAILABLE");

        verify(scheduleRepository).save(schedule);
    }

    @Test
    void testUpdateScheduleStatusInvalidStatus() {
        UUID scheduleId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.updateScheduleStatus(scheduleId, "INVALID_STATUS")
        );
    }

    @Test
    void testUpdateScheduleStatusScheduleNotFound() {
        UUID scheduleId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.updateScheduleStatus(scheduleId, "AVAILABLE")
        );
    }
}