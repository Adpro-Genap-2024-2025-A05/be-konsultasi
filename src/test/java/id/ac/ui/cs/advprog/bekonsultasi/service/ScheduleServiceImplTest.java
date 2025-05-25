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
import io.micrometer.core.instrument.Counter;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private KonsultasiRepository konsultasiRepository;
    @Mock private ScheduleFactory scheduleFactory;
    @Mock private Counter scheduleCreatedCounter;
    @Mock private Counter scheduleOneTimeCreatedCounter;
    @Mock private Counter scheduleUpdatedCounter;
    @Mock private Counter scheduleDeletedCounter;
    @Mock private Counter scheduleDeleteAsyncCounter;
    @Mock private Counter scheduleAvailabilityCheckCounter;
    @Mock private Counter scheduleAvailableTimesRequestCounter;
    @Mock private Counter scheduleCaregiverQueryCounter;
    @Mock private Counter scheduleAllQueryCounter;
    @Mock private Counter scheduleAvailableByIdCounter;
    @Mock private Counter scheduleAvailableMultipleCounter;
    @Mock private Counter scheduleConflictCounter;
    @Mock private Counter scheduleValidationErrorCounter;
    @Mock private Counter scheduleAuthorizationErrorCounter;
    @Mock private Counter scheduleNotFoundCounter;
    @Mock private Counter scheduleTimeValidationErrorCounter;
    @Mock private Counter schedulePastDateErrorCounter;
    @Mock private Counter scheduleActiveKonsultasiBlockCounter;
    @Mock private Counter scheduleDatabaseErrorCounter;
    @Mock private Counter scheduleGeneralErrorCounter;
    @Mock private Counter scheduleOverlapPreventedCounter;
    @Mock private Counter scheduleWeeklyScheduleCounter;
    @Mock private Counter scheduleFactoryRegularCounter;
    @Mock private Counter scheduleFactoryOneTimeCounter;
    @Mock private Counter scheduleSuccessfulOperationsCounter;
    @Mock private Counter scheduleFailedOperationsCounter;

    @InjectMocks private ScheduleServiceImpl scheduleService;

    private UUID caregiverId;
    private UUID scheduleId;
    private CreateScheduleDto createScheduleDto;
    private CreateOneTimeScheduleDto createOneTimeScheduleDto;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        createScheduleDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        createOneTimeScheduleDto = CreateOneTimeScheduleDto.builder()
                .specificDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        schedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .oneTime(false)
                .build();
    }

    @Test
    void createSchedule_Success() {
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Collections.emptyList());
        when(scheduleFactory.createSchedule(createScheduleDto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
        assertEquals(scheduleId, result.getId());
    }

    @Test
    void createSchedule_InvalidTimes_ThrowsException() {
        CreateScheduleDto invalidDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.createSchedule(invalidDto, caregiverId));
    }

    @Test
    void createSchedule_OverlappingSchedule_ThrowsScheduleConflictException() {
        Schedule overlapping = Schedule.builder()
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .build();
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(overlapping));

        assertThrows(ScheduleConflictException.class,
                () -> scheduleService.createSchedule(createScheduleDto, caregiverId));
    }

    @Test
    void createSchedule_DatabaseError_ThrowsException() {
        when(scheduleRepository.findByCaregiverId(caregiverId)).thenReturn(Collections.emptyList());
        when(scheduleFactory.createSchedule(createScheduleDto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> scheduleService.createSchedule(createScheduleDto, caregiverId));
    }

    @Test
    void createOneTimeSchedule_Success() {
        Schedule oneTimeSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .specificDate(LocalDate.now().plusDays(1))
                .oneTime(true)
                .build();
        when(scheduleFactory.createOneTimeSchedule(createOneTimeScheduleDto, caregiverId))
                .thenReturn(oneTimeSchedule);
        when(scheduleRepository.save(oneTimeSchedule)).thenReturn(oneTimeSchedule);

        ScheduleResponseDto result = scheduleService.createOneTimeSchedule(createOneTimeScheduleDto, caregiverId);

        assertNotNull(result);
        assertTrue(result.isOneTime());
    }

    @Test
    void createOneTimeSchedule_PastDate_ThrowsException() {
        CreateOneTimeScheduleDto pastDateDto = CreateOneTimeScheduleDto.builder()
                .specificDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.createOneTimeSchedule(pastDateDto, caregiverId));
    }

    @Test
    void createOneTimeSchedule_InvalidTimes_ThrowsException() {
        CreateOneTimeScheduleDto invalidDto = CreateOneTimeScheduleDto.builder()
                .specificDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.createOneTimeSchedule(invalidDto, caregiverId));
    }

    @Test
    void createOneTimeSchedule_DatabaseError_ThrowsException() {
        when(scheduleFactory.createOneTimeSchedule(createOneTimeScheduleDto, caregiverId))
                .thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> scheduleService.createOneTimeSchedule(createOneTimeScheduleDto, caregiverId));
    }

    @Test
    void updateSchedule_Success() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(Collections.emptyList());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.updateSchedule(scheduleId, createScheduleDto, caregiverId);

        assertNotNull(result);
    }

    @Test
    void updateSchedule_UnauthorizedCaregiver_ThrowsAuthenticationException() {
        UUID wrongCaregiverId = UUID.randomUUID();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(AuthenticationException.class,
                () -> scheduleService.updateSchedule(scheduleId, createScheduleDto, wrongCaregiverId));
    }

    @Test
    void updateSchedule_WithActiveKonsultations_ThrowsScheduleException() {
        Konsultasi activeKonsultasi = Konsultasi.builder()
                .status("CONFIRMED")
                .scheduleDateTime(LocalDateTime.now().plusDays(1))
                .build();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId))
                .thenReturn(Collections.singletonList(activeKonsultasi));

        assertThrows(ScheduleException.class,
                () -> scheduleService.updateSchedule(scheduleId, createScheduleDto, caregiverId));
    }

    @Test
    void updateSchedule_InvalidTimes_ThrowsException() {
        CreateScheduleDto invalidDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.updateSchedule(scheduleId, invalidDto, caregiverId));
    }

    @Test
    void deleteSchedule_Success() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(Collections.emptyList());

        scheduleService.deleteScheduleAsync(scheduleId, caregiverId);

        verify(scheduleRepository).deleteById(scheduleId);
    }

    @Test
    void deleteSchedule_UnauthorizedCaregiver_ThrowsAuthenticationException() {
        UUID wrongCaregiverId = UUID.randomUUID();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        CompletableFuture<Void> result = scheduleService.deleteScheduleAsync(scheduleId, wrongCaregiverId);

        assertTrue(result.isCompletedExceptionally());

        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(AuthenticationException.class, exception.getCause());
        assertEquals("You can only delete your own schedules", exception.getCause().getMessage());
    }

    @Test
    void deleteSchedule_WithFutureKonsultations_ThrowsScheduleException() {
        Konsultasi futureKonsultasi = Konsultasi.builder()
                .status("CONFIRMED")
                .scheduleDateTime(LocalDateTime.now().plusDays(1))
                .build();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId))
                .thenReturn(Collections.singletonList(futureKonsultasi));

        CompletableFuture<Void> result = scheduleService.deleteScheduleAsync(scheduleId, caregiverId);

        assertTrue(result.isCompletedExceptionally());

        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(ScheduleException.class, exception.getCause());
        assertEquals("Cannot delete schedule with future consultations", exception.getCause().getMessage());
    }

    @Test
    void deleteScheduleAsync_Success() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenReturn(Collections.emptyList());

        CompletableFuture<Void> result = scheduleService.deleteScheduleAsync(scheduleId, caregiverId);

        assertNotNull(result);
    }

    @Test
    void deleteScheduleAsync_WithException_ReturnsFailedFuture() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.findByScheduleId(scheduleId)).thenThrow(new RuntimeException("Error"));

        CompletableFuture<Void> result = scheduleService.deleteScheduleAsync(scheduleId, caregiverId);

        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void getCaregiverSchedules_Success() {
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(schedule));

        List<ScheduleResponseDto> result = scheduleService.getCaregiverSchedules(caregiverId);

        assertEquals(1, result.size());
    }

    @Test
    void getCaregiverSchedules_DatabaseError_ThrowsException() {
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> scheduleService.getCaregiverSchedules(caregiverId));
    }

    @Test
    void getAllSchedules_Success() {
        when(scheduleRepository.findAll()).thenReturn(Collections.singletonList(schedule));

        List<ScheduleResponseDto> result = scheduleService.getAllSchedules();

        assertEquals(1, result.size());
    }

    @Test
    void getAllSchedules_DatabaseError_ThrowsException() {
        when(scheduleRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> scheduleService.getAllSchedules());
    }

    @Test
    void isScheduleAvailableForDateTime_Success() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        boolean result = scheduleService.isScheduleAvailableForDateTime(scheduleId, LocalDateTime.now());

        assertTrue(result);
    }

    @Test
    void isScheduleAvailableForDateTime_ScheduleNotFound_ThrowsException() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.isScheduleAvailableForDateTime(scheduleId, LocalDateTime.now()));
    }

    @Test
    void getAvailableDateTimesForSchedule_Success() {
        List<LocalDateTime> result = scheduleService.getAvailableDateTimesForSchedule(scheduleId, 4);

        assertNotNull(result);
    }

    @Test
    void getAvailableSchedulesByCaregiver_Success() {
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(schedule));

        List<ScheduleResponseDto> result = scheduleService.getAvailableSchedulesByCaregiver(caregiverId);

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableSchedulesByCaregiver_FiltersPastOneTimeSchedules() {
        Schedule pastOneTime = Schedule.builder()
                .caregiverId(caregiverId)
                .specificDate(LocalDate.now().minusDays(1))
                .oneTime(true)
                .build();
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Arrays.asList(schedule, pastOneTime));

        List<ScheduleResponseDto> result = scheduleService.getAvailableSchedulesByCaregiver(caregiverId);

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableSchedulesByCaregiver_DatabaseError_ThrowsException() {
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> scheduleService.getAvailableSchedulesByCaregiver(caregiverId));
    }

    @Test
    void getAvailableSchedulesForCaregivers_Success() {
        List<UUID> caregiverIds = Collections.singletonList(caregiverId);
        when(scheduleRepository.findByCaregiverIdIn(caregiverIds))
                .thenReturn(Collections.singletonList(schedule));

        List<ScheduleResponseDto> result = scheduleService.getAvailableSchedulesForCaregivers(caregiverIds);

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableSchedulesForCaregivers_DatabaseError_ThrowsException() {
        List<UUID> caregiverIds = Collections.singletonList(caregiverId);
        when(scheduleRepository.findByCaregiverIdIn(caregiverIds))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> scheduleService.getAvailableSchedulesForCaregivers(caregiverIds));
    }

    @Test
    void findScheduleById_Success() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.findScheduleById(scheduleId);

        assertEquals(scheduleId, result.getId());
    }

    @Test
    void findScheduleById_NotFound_ThrowsException() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> scheduleService.findScheduleById(scheduleId));
    }

    @Test
    void findScheduleById_DatabaseError_ThrowsException() {
        when(scheduleRepository.findById(scheduleId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> scheduleService.findScheduleById(scheduleId));
    }

    @Test
    void timeOverlapDetection_ExactMatch_ThrowsConflict() {
        Schedule exactMatch = Schedule.builder()
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(exactMatch));

        assertThrows(ScheduleConflictException.class,
                () -> scheduleService.createSchedule(createScheduleDto, caregiverId));
    }

    @Test
    void timeOverlapDetection_NoOverlap_Success() {
        Schedule noOverlap = Schedule.builder()
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(noOverlap));
        when(scheduleFactory.createSchedule(createScheduleDto, caregiverId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        ScheduleResponseDto result = scheduleService.createSchedule(createScheduleDto, caregiverId);

        assertNotNull(result);
    }

    @Test
    void isScheduleCurrentlyAvailable_OneTimeScheduleWithNullDate() {
        Schedule nullDateSchedule = Schedule.builder()
                .caregiverId(caregiverId)
                .specificDate(null)
                .oneTime(true)
                .build();
        when(scheduleRepository.findByCaregiverId(caregiverId))
                .thenReturn(Collections.singletonList(nullDateSchedule));

        List<ScheduleResponseDto> result = scheduleService.getAvailableSchedulesByCaregiver(caregiverId);

        assertTrue(result.isEmpty());
    }
}