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
    private KonsultasiRepository konsultasiRepository;

    @Mock
    private ScheduleFactory scheduleFactory;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private UUID caregiverId;
    private UUID scheduleId;
    private Schedule schedule;
    private Schedule unavailableSchedule;
    private CreateScheduleDto createScheduleDto;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        createScheduleDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        schedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
        schedule.setState(new AvailableState());

        unavailableSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("UNAVAILABLE")
                .build();
        unavailableSchedule.setState(new UnavailableState());
    }

    @Test
    void testCreateSchedule() {
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Collections.emptyList());
        when(scheduleFactory.createSchedule(createScheduleDto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        assertEquals(scheduleId, result.getId());
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
    void testUpdateScheduleSuccess() {
        UUID scheduleId = UUID.randomUUID();
        CreateScheduleDto updateDto = CreateScheduleDto.builder()
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status("AVAILABLE")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Collections.emptyList());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(updatedSchedule);

        ScheduleResponseDto result = scheduleService.updateSchedule(scheduleId, updateDto, caregiverId);

        assertNotNull(result);
        assertEquals(scheduleId, result.getId());
        assertEquals(DayOfWeek.TUESDAY, result.getDay());
        assertEquals(LocalTime.of(14, 0), result.getStartTime());
        assertEquals(LocalTime.of(15, 0), result.getEndTime());

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository).findByCaregiverId(caregiverId);
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testUpdateScheduleNotOwner() {
        UUID scheduleId = UUID.randomUUID();
        UUID differentCaregiverId = UUID.randomUUID();

        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));

        assertThrows(AuthenticationException.class, () ->
                scheduleService.updateSchedule(scheduleId, createScheduleDto, differentCaregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testUpdateScheduleUnavailableWithActiveKonsultasi() {
        Schedule unavailableSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("UNAVAILABLE")
                .build();

        Konsultasi activeKonsultasi = Konsultasi.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .status("CONFIRMED")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(unavailableSchedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(List.of(activeKonsultasi));

        assertThrows(ScheduleException.class, () ->
                scheduleService.updateSchedule(scheduleId, createScheduleDto, caregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(konsultasiRepository).findByScheduleId(scheduleId);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testUpdateScheduleInvalidTimes() {
        CreateScheduleDto invalidDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.updateSchedule(scheduleId, invalidDto, caregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testUpdateScheduleScheduleConflict() {
        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        Schedule conflictingSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status("AVAILABLE")
                .build();

        CreateScheduleDto updateDto = CreateScheduleDto.builder()
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(15, 30))
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(List.of(conflictingSchedule));

        assertThrows(ScheduleConflictException.class, () ->
                scheduleService.updateSchedule(scheduleId, updateDto, caregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository).findByCaregiverId(caregiverId);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testDeleteScheduleSuccess() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(Collections.emptyList());
        doNothing().when(scheduleRepository).deleteById(scheduleId);

        scheduleService.deleteSchedule(scheduleId, caregiverId);

        verify(scheduleRepository).findById(scheduleId);
        verify(konsultasiRepository).findByScheduleId(scheduleId);
        verify(scheduleRepository).deleteById(scheduleId);
    }

    @Test
    void testDeleteScheduleNotOwner() {
        UUID differentCaregiverId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(AuthenticationException.class, () ->
                scheduleService.deleteSchedule(scheduleId, differentCaregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteScheduleWithActiveKonsultasi() {
        Konsultasi activeKonsultasi = Konsultasi.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .status("CONFIRMED")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(List.of(activeKonsultasi));

        assertThrows(ScheduleException.class, () ->
                scheduleService.deleteSchedule(scheduleId, caregiverId)
        );

        verify(scheduleRepository).findById(scheduleId);
        verify(konsultasiRepository).findByScheduleId(scheduleId);
        verify(scheduleRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteScheduleWithCompletedKonsultasi() {
        Konsultasi completedKonsultasi = Konsultasi.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .status("DONE")
                .build();

        Konsultasi cancelledKonsultasi = Konsultasi.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .status("CANCELLED")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId))
                .thenReturn(Arrays.asList(completedKonsultasi, cancelledKonsultasi));
        doNothing().when(scheduleRepository).deleteById(scheduleId);

        scheduleService.deleteSchedule(scheduleId, caregiverId);

        verify(scheduleRepository).findById(scheduleId);
        verify(konsultasiRepository).findByScheduleId(scheduleId);
        verify(scheduleRepository).deleteById(scheduleId);
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
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDay());
        assertEquals(DayOfWeek.TUESDAY, result.get(1).getDay());

        verify(scheduleRepository).findByCaregiverId(caregiverId);
    }

    @Test
    void testGetAllSchedules() {
        UUID caregiverId2 = UUID.randomUUID();

        Schedule schedule1 = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId2)
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status("AVAILABLE")
                .build();

        when(scheduleRepository.findAll()).thenReturn(Arrays.asList(schedule1, schedule2));

        List<ScheduleResponseDto> result = scheduleService.getAllSchedules();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(caregiverId, result.get(0).getCaregiverId());
        assertEquals(caregiverId2, result.get(1).getCaregiverId());

        verify(scheduleRepository).findAll();
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

        assertEquals("AVAILABLE", unavailableSchedule.getStatus());
        verify(scheduleRepository).save(unavailableSchedule);
    }

    @Test
    void testUpdateScheduleStatusToUnavailable() {
        UUID scheduleId = UUID.randomUUID();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        scheduleService.updateScheduleStatus(scheduleId, "UNAVAILABLE");

        assertEquals("UNAVAILABLE", schedule.getStatus());
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