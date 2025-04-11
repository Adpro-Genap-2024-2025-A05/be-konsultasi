package id.ac.ui.cs.advprog.bekonsultasi.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleTest {

    @Test
    public void testScheduleCreation() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        String day = "Monday";
        String time = "10:00";
        String status = "Available";

        // Act
        Schedule schedule = Schedule.builder()
                .id(id)
                .caregiverId(caregiverId)
                .day(day)
                .time(time)
                .status(status)
                .build();

        // Assert
        assertEquals(id, schedule.getId());
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(day, schedule.getDay());
        assertEquals(time, schedule.getTime());
        assertEquals(status, schedule.getStatus());
    }

    @Test
    public void testChangeStatus() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(UUID.randomUUID())
                .day("Monday")
                .time("10:00")
                .status("Available")
                .build();

        // Act
        schedule.changeStatus("Booked");

        // Assert
        assertEquals("Booked", schedule.getStatus());
        assertTrue(schedule.getState() instanceof BookedState);
    }
}