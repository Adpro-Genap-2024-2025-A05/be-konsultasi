package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    public void whenFindByCaregiverId_thenReturnScheduleList() {
        // Arrange
        UUID caregiverId = UUID.randomUUID();

        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Monday")
                .time("09:00")
                .status("Available")
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Tuesday")
                .time("10:00")
                .status("Booked")
                .build();

        entityManager.persist(schedule1);
        entityManager.persist(schedule2);
        entityManager.flush();

        // Act
        List<Schedule> foundSchedules = scheduleRepository.findByCaregiverId(caregiverId);

        // Assert
        assertEquals(2, foundSchedules.size());
    }

    @Test
    public void whenFindByCaregiverIdAndStatus_thenReturnFilteredSchedules() {
        // Arrange
        UUID caregiverId = UUID.randomUUID();

        Schedule availableSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Wednesday")
                .time("14:00")
                .status("Available")
                .build();

        Schedule bookedSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day("Thursday")
                .time("15:00")
                .status("Booked")
                .build();

        entityManager.persist(availableSchedule);
        entityManager.persist(bookedSchedule);
        entityManager.flush();

        // Act
        List<Schedule> availableSchedules = scheduleRepository.findByCaregiverIdAndStatus(caregiverId, "Available");

        // Assert
        assertEquals(1, availableSchedules.size());
        assertEquals("Available", availableSchedules.get(0).getStatus());
    }
}