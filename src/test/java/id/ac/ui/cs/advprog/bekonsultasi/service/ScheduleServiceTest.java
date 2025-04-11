package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.factory.ScheduleFactoryImpl;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScheduleServiceTest {

    private ScheduleRepository scheduleRepository;
    private ScheduleFactoryImpl scheduleFactory;
    private ScheduleService scheduleService;  // Interface type here

    private UUID caregiverId;
    private UUID scheduleId;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        scheduleRepository = Mockito.mock(ScheduleRepository.class);
        scheduleFactory = Mockito.mock(ScheduleFactoryImpl.class);
        scheduleService = new ScheduleServiceImpl(scheduleRepository, scheduleFactory);  // Concrete implementation

        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        testSchedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day("Friday")
                .time("09:00")
                .status("Available")
                .build();
    }

    @Test
    void whenCreateSchedule_thenReturnSavedSchedule() {
        // Arrange
        when(scheduleFactory.createAvailableSchedule(any(), any(), any())).thenReturn(testSchedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Act
        Schedule result = scheduleService.createSchedule(caregiverId, "Friday", "09:00");

        // Assert
        assertNotNull(result);
        assertEquals(scheduleId, result.getId());
        verify(scheduleFactory).createAvailableSchedule(caregiverId, "Friday", "09:00");
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void whenGetAvailableSchedules_thenReturnAvailableSchedulesList() {
        // Arrange
        List<Schedule> expectedSchedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByCaregiverIdAndStatus(caregiverId, "Available"))
                .thenReturn(expectedSchedules);

        // Act
        List<Schedule> result = scheduleService.getAvailableSchedules(caregiverId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(scheduleId, result.get(0).getId());
        verify(scheduleRepository).findByCaregiverIdAndStatus(caregiverId, "Available");
    }

    @Test
    void whenGetScheduleByIdWithValidId_thenReturnSchedule() {
        // Arrange
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

        // Act
        Schedule result = scheduleService.getScheduleById(scheduleId);

        // Assert
        assertNotNull(result);
        assertEquals(scheduleId, result.getId());
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void whenGetScheduleByIdWithInvalidId_thenThrowException() {
        // Arrange
        when(scheduleRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> scheduleService.getScheduleById(UUID.randomUUID()));
    }

    @Test
    void whenUpdateScheduleStatus_thenScheduleStatusIsUpdated() {
        // Arrange
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // Act
        scheduleService.updateScheduleStatus(scheduleId, "Booked");

        // Assert
        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleRepository).save(testSchedule);
    }
}