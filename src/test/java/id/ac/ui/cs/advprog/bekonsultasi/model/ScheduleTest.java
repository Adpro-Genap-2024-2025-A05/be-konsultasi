package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {
    private Schedule schedule;
    private UUID caregiverId;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        schedule = Schedule.builder()
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        schedule.setState(new AvailableState());
    }

    @Test
    void testInitialStateIsAvailable() {
        assertEquals("AVAILABLE", schedule.getStatus());
        assertTrue(schedule.getState() instanceof AvailableState);
    }

    @Test
    void testApproveFromAvailableState() {
        schedule.approve();
        assertEquals("APPROVED", schedule.getStatus());
        assertTrue(schedule.getState() instanceof ApprovedState);
    }

    @Test
    void testCannotApproveApprovedState() {
        schedule.approve();
        assertThrows(IllegalStateException.class, () -> schedule.approve());
    }

    @Test
    void testStatePersistsAfterSerialization() {
        schedule.approve();
        Schedule newSchedule = Schedule.builder()
                .id(schedule.getId())
                .caregiverId(schedule.getCaregiverId())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus())
                .build();
        newSchedule.setState(new ApprovedState());
        assertEquals("APPROVED", newSchedule.getStatus());
    }

    @Test
    void testSetStateUpdatesStatus() {
        schedule.setState(new ApprovedState());
        assertEquals("APPROVED", schedule.getStatus());
    }
}