package id.ac.ui.cs.advprog.bekonsultasi.factory;

import id.ac.ui.cs.advprog.bekonsultasi.model.schedulestate.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.schedulestate.UnavailableState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleFactoryTest {

    private ScheduleFactory scheduleFactory;
    private UUID caregiverId;

    @BeforeEach
    void setUp() {
        scheduleFactory = new ScheduleFactoryImpl();
        caregiverId = UUID.randomUUID();
    }

    @Test
    void whenCreatingAvailableSchedule_thenStatusIsAvailable() {
        // Act
        Schedule schedule = scheduleFactory.createAvailableSchedule(caregiverId, "Tuesday", "14:00");

        // Assert
        assertNotNull(schedule);
        assertNotNull(schedule.getId());
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals("Tuesday", schedule.getDay());
        assertEquals("14:00", schedule.getTime());
        assertEquals("Available", schedule.getStatus());
        assertTrue(schedule.getState() instanceof AvailableState);
    }

    @Test
    void whenCreatingUnavailableSchedule_thenStatusIsUnavailable() {
        // Act
        Schedule schedule = scheduleFactory.createUnavailableSchedule(caregiverId, "Wednesday", "15:00");

        // Assert
        assertNotNull(schedule);
        assertEquals("Unavailable", schedule.getStatus());
        assertTrue(schedule.getState() instanceof UnavailableState);
    }
}