package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.*;
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
import java.util.*;

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

    private CreateScheduleDto createScheduleDto;
    private Schedule schedule;
    private UUID caregiverId;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();

        createScheduleDto = new CreateScheduleDto();
        createScheduleDto.setDay(DayOfWeek.MONDAY);
        createScheduleDto.setStartTime(LocalTime.of(9, 0));
        createScheduleDto.setEndTime(LocalTime.of(10, 0));

        schedule = new Schedule();
        schedule.setId(UUID.randomUUID());
        schedule.setCaregiverId(caregiverId);
        schedule.setDay(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setStatus("AVAILABLE");
    }

    @Test
    void testCreateScheduleSuccess() {
        when(scheduleFactory.createSchedule(any(), any())).thenReturn(schedule);
        when(scheduleRepository.save(any())).thenReturn(schedule);
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.emptyList());

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        assertEquals(schedule.getId(), result.getId());
        assertEquals(caregiverId, result.getCaregiverId());
        assertEquals("AVAILABLE", result.getStatus());

        verify(scheduleRepository).findByCaregiverId(caregiverId);
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void testCreateScheduleWithInvalidTime() {
        createScheduleDto.setEndTime(LocalTime.of(8, 0));

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.createSchedule(createScheduleDto, caregiverId));
    }

    @Test
    void testCreateScheduleWithConflict() {
        Schedule existingSchedule = new Schedule();
        existingSchedule.setDay(DayOfWeek.MONDAY);
        existingSchedule.setStartTime(LocalTime.of(9, 30));
        existingSchedule.setEndTime(LocalTime.of(10, 30));

        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(existingSchedule));

        assertThrows(ScheduleConflictException.class,
                () -> scheduleService.createSchedule(createScheduleDto, caregiverId));
    }

    @Test
    void testCreateScheduleWithNoConflictDifferentDay() {
        Schedule existingSchedule = new Schedule();
        existingSchedule.setDay(DayOfWeek.TUESDAY);
        existingSchedule.setStartTime(LocalTime.of(9, 0));
        existingSchedule.setEndTime(LocalTime.of(10, 0));

        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(existingSchedule));
        when(scheduleFactory.createSchedule(any(), any())).thenReturn(schedule);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        verify(scheduleRepository).save(any());
    }

    @Test
    void testCreateScheduleWithNoConflictDifferentTime() {
        Schedule existingSchedule = new Schedule();
        existingSchedule.setDay(DayOfWeek.MONDAY);
        existingSchedule.setStartTime(LocalTime.of(11, 0));
        existingSchedule.setEndTime(LocalTime.of(12, 0));

        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(existingSchedule));
        when(scheduleFactory.createSchedule(any(), any())).thenReturn(schedule);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        verify(scheduleRepository).save(any());
    }

    @Test
    void testGetCaregiverSchedules() {
        Schedule available = new Schedule();
        available.setStatus("AVAILABLE");

        Schedule requested = new Schedule();
        requested.setStatus("REQUESTED");

        Schedule approved = new Schedule();
        approved.setStatus("APPROVED");

        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(available, requested, approved));

        List<ScheduleResponseDto> results = scheduleService.getCaregiverSchedules(caregiverId);

        assertEquals(3, results.size());
        assertEquals("AVAILABLE", results.get(0).getStatus());
        assertEquals("REQUESTED", results.get(1).getStatus());
        assertEquals("APPROVED", results.get(2).getStatus());
    }

    @Test
    void testGetCaregiverSchedulesWithUnknownStatus() {
        Schedule unknown = new Schedule();
        unknown.setStatus("UNKNOWN");

        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(unknown));

        assertThrows(IllegalStateException.class,
                () -> scheduleService.getCaregiverSchedules(caregiverId));
    }

    @Test
    void testConvertToDtoThroughPublicMethod() {
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(List.of(schedule));

        List<ScheduleResponseDto> results = scheduleService.getCaregiverSchedules(caregiverId);
        ScheduleResponseDto dto = results.get(0);

        assertEquals(schedule.getId(), dto.getId());
        assertEquals(schedule.getCaregiverId(), dto.getCaregiverId());
        assertEquals(schedule.getDay(), dto.getDay());
        assertEquals(schedule.getStartTime(), dto.getStartTime());
        assertEquals(schedule.getEndTime(), dto.getEndTime());
        assertEquals(schedule.getStatus(), dto.getStatus());
    }

    @Test
    void testUpdateScheduleStatus() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setStatus("AVAILABLE");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenReturn(schedule);

        scheduleService.updateScheduleStatus(scheduleId, "REQUESTED");

        assertEquals("REQUESTED", schedule.getStatus());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void testUpdateScheduleStatusNotFound() {
        UUID scheduleId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.updateScheduleStatus(scheduleId, "REQUESTED"));
    }

    @Test
    void testUpdateScheduleStatusInvalid() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = new Schedule();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.updateScheduleStatus(scheduleId, "INVALID_STATUS"));
    }
}