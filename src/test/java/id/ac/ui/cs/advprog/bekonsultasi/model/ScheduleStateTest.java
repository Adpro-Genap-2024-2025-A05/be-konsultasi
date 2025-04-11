package id.ac.ui.cs.advprog.bekonsultasi.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleStateTest {

    @Test
    public void whenAvailableScheduleIsBooked_thenStatusChangesToBooked() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(UUID.randomUUID())
                .day("Monday")
                .time("10:00")
                .status("Available")
                .build();

        schedule.setState(new AvailableState());

        // Act
        schedule.getState().book(schedule);

        // Assert
        assertEquals("Booked", schedule.getStatus());
        assertTrue(schedule.getState() instanceof BookedState);
    }

    @Test
    public void whenBookedScheduleIsBookedAgain_thenThrowException() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(UUID.randomUUID())
                .day("Monday")
                .time("10:00")
                .status("Booked")
                .build();

        schedule.setState(new BookedState());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> schedule.getState().book(schedule));
    }

    @Test
    public void whenUnavailableScheduleIsBooked_thenThrowException() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(UUID.randomUUID())
                .day("Tuesday")
                .time("14:00")
                .status("Unavailable")
                .build();

        schedule.setState(new UnavailableState());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> schedule.getState().book(schedule));
    }
}