package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleRepositoryTest {

    private ScheduleRepository scheduleRepository;
    private UUID caregiverId;
    private UUID scheduleId;

    @BeforeEach
    void setUp() {
        scheduleRepository = new ScheduleRepository();
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
    }

    @Test
    void whenSavingSchedule_thenCanRetrieveById() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day("Monday")
                .time("10:00")
                .status("Available")
                .build();

        // Act
        scheduleRepository.save(schedule);
        Optional<Schedule> found = scheduleRepository.findById(scheduleId);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(scheduleId, found.get().getId());
    }

    @Test
    void whenFindingByCaregiver_thenReturnsSchedulesForThatCaregiver() {
        // Arrange
        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Monday")
                .time("10:00")
                .status("Available")
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Tuesday")
                .time("11:00")
                .status("Booked")
                .build();

        Schedule schedule3 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(UUID.randomUUID()) // Different caregiver
                .day("Wednesday")
                .time("12:00")
                .status("Available")
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);

        // Act
        List<Schedule> found = scheduleRepository.findByCaregiverId(caregiverId);

        // Assert
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s -> s.getCaregiverId().equals(caregiverId)));
    }

    @Test
    void whenFindingByCaregiverAndStatus_thenReturnsFilteredSchedules() {
        // Arrange
        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Monday")
                .time("10:00")
                .status("Available")
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Tuesday")
                .time("11:00")
                .status("Booked")
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);

        // Act
        List<Schedule> availableSchedules = scheduleRepository.findByCaregiverIdAndStatus(caregiverId, "Available");

        // Assert
        assertEquals(1, availableSchedules.size());
        assertEquals("Available", availableSchedules.get(0).getStatus());
    }
}