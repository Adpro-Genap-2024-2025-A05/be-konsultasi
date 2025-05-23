package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.KonsultasiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.RescheduleKonsultasiDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Schedule oneTimeSchedule;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        schedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .oneTime(false)
                .build();

        oneTimeSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .specificDate(LocalDate.of(2025, 6, 2))
                .oneTime(true)
                .build();
    }

    @Test
    void testCreateSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setDay(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        when(scheduleFactory.createSchedule(dto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(new ArrayList<>());

        ScheduleResponseDto response = scheduleService.createSchedule(dto, caregiverId);

        assertNotNull(response);
        assertEquals(scheduleId, response.getId());
        assertEquals(caregiverId, response.getCaregiverId());
        assertEquals(DayOfWeek.MONDAY, response.getDay());
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(11, 0), response.getEndTime());
        assertFalse(response.isOneTime());

        verify(scheduleRepository).save(schedule);
    }

    @Test
    void testCreateOneTimeSchedule() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        dto.setSpecificDate(LocalDate.of(2025, 6, 2));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        when(scheduleFactory.createOneTimeSchedule(dto, caregiverId)).thenReturn(oneTimeSchedule);
        when(scheduleRepository.save(oneTimeSchedule)).thenReturn(oneTimeSchedule);
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(new ArrayList<>());

        ScheduleResponseDto response = scheduleService.createOneTimeSchedule(dto, caregiverId);

        assertNotNull(response);
        assertEquals(oneTimeSchedule.getId(), response.getId());
        assertEquals(caregiverId, response.getCaregiverId());
        assertEquals(DayOfWeek.MONDAY, response.getDay());
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(11, 0), response.getEndTime());
        assertEquals(LocalDate.of(2025, 6, 2), response.getSpecificDate());
        assertTrue(response.isOneTime());

        verify(scheduleRepository).save(oneTimeSchedule);
    }

    @Test
    void testCreateOneTimeScheduleInPast() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        dto.setSpecificDate(LocalDate.of(2020, 1, 1));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createOneTimeSchedule(dto, caregiverId));
    }

    @Test
    void testCreateOneTimeScheduleWithConflict() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        dto.setSpecificDate(LocalDate.of(2025, 6, 2));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        List<Schedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(oneTimeSchedule);

        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(existingSchedules);

        assertThrows(ScheduleConflictException.class, () ->
                scheduleService.createOneTimeSchedule(dto, caregiverId));
    }

    @Test
    void testIsScheduleAvailableForDateTime_RecurringSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(new ArrayList<>());

        LocalDateTime dateTime = LocalDateTime.of(
                LocalDate.now().with(DayOfWeek.MONDAY),
                LocalTime.of(10, 30));

        boolean isAvailable = scheduleService.isScheduleAvailableForDateTime(scheduleId, dateTime);
        assertTrue(isAvailable);
    }

    @Test
    void testIsScheduleAvailableForDateTime_OneTimeSchedule() {
        UUID oneTimeId = oneTimeSchedule.getId();
        when(scheduleRepository.findById(oneTimeId)).thenReturn(Optional.of(oneTimeSchedule));
        when(konsultasiRepository.findByScheduleId(oneTimeId)).thenReturn(new ArrayList<>());

        LocalDateTime correctDateTime = LocalDateTime.of(
                LocalDate.of(2025, 6, 2),
                LocalTime.of(10, 30));

        boolean isAvailable = scheduleService.isScheduleAvailableForDateTime(oneTimeId, correctDateTime);
        assertTrue(isAvailable);

        LocalDateTime wrongDateTime = LocalDateTime.of(
                LocalDate.of(2025, 6, 9),
                LocalTime.of(10, 30));

        isAvailable = scheduleService.isScheduleAvailableForDateTime(oneTimeId, wrongDateTime);
        assertFalse(isAvailable);
    }

    @Test
    void testGetAvailableDateTimesForSchedule_RecurringSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(new ArrayList<>());

        List<LocalDateTime> availableTimes = scheduleService.getAvailableDateTimesForSchedule(scheduleId, 4);

        assertNotNull(availableTimes);
        assertFalse(availableTimes.isEmpty());
        assertEquals(4, availableTimes.size());

        for (LocalDateTime dateTime : availableTimes) {
            assertEquals(DayOfWeek.MONDAY, dateTime.getDayOfWeek());
            assertEquals(LocalTime.of(10, 0), dateTime.toLocalTime());
        }
    }

    @Test
    void testGetAvailableDateTimesForSchedule_OneTimeSchedule() {
        UUID oneTimeId = oneTimeSchedule.getId();
        when(scheduleRepository.findById(oneTimeId)).thenReturn(Optional.of(oneTimeSchedule));
        when(konsultasiRepository.findByScheduleId(oneTimeId)).thenReturn(new ArrayList<>());

        List<LocalDateTime> availableTimes = scheduleService.getAvailableDateTimesForSchedule(oneTimeId, 4);

        assertNotNull(availableTimes);
        assertEquals(1, availableTimes.size());

        LocalDateTime dateTime = availableTimes.get(0);
        assertEquals(LocalDate.of(2025, 6, 2), dateTime.toLocalDate());
        assertEquals(LocalTime.of(10, 0), dateTime.toLocalTime());
    }

    @Test
    void testDeleteSchedule_WithActiveKonsultasi() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        List<Konsultasi> activeKonsultasi = new ArrayList<>();
        Konsultasi konsultasi = Konsultasi.builder()
                .scheduleId(scheduleId)
                .caregiverId(caregiverId)
                .pacilianId(UUID.randomUUID())
                .scheduleDateTime(LocalDateTime.now().plusDays(1))
                .status("REQUESTED")
                .build();
        activeKonsultasi.add(konsultasi);

        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(activeKonsultasi);

        assertThrows(ScheduleException.class, () ->
                scheduleService.deleteSchedule(scheduleId, caregiverId));

        verify(scheduleRepository, never()).deleteById(scheduleId);
    }

    @Test
    void testIsScheduleCurrentlyAvailable_OneTimeScheduleToday() {
        Schedule todayOneTime = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .specificDate(LocalDate.now())
                .oneTime(true)
                .build();

        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Arrays.asList(todayOneTime));

        List<ScheduleResponseDto> result = scheduleService.getAvailableSchedulesByCaregiver(caregiverId);

        assertEquals(0, result.size());
    }

    @Test
    void testValidateScheduleTimes_SameStartAndEndTime() {
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setDay(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(10, 0));

        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(new ArrayList<>());
        when(scheduleFactory.createSchedule(dto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(dto, caregiverId);

        assertNotNull(result);
    }
}