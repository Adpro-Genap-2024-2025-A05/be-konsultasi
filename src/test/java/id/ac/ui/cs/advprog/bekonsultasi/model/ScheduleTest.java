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
    private UUID patientId;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        patientId = UUID.randomUUID();
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
    void testRequestChangesStateToRequested() {
        schedule.request(patientId);
        assertEquals("REQUESTED", schedule.getStatus());
        assertTrue(schedule.getState() instanceof RequestedState);
        assertEquals(patientId, schedule.getPatientId());
    }

    @Test
    void testApproveFromRequestedState() {
        schedule.request(patientId);
        schedule.approve();
        assertEquals("APPROVED", schedule.getStatus());
        assertTrue(schedule.getState() instanceof ApprovedState);
    }

    @Test
    void testRejectFromRequestedState() {
        schedule.request(patientId);
        schedule.reject();
        assertEquals("REJECTED", schedule.getStatus());
        assertTrue(schedule.getState() instanceof RejectedState);
    }

    @Test
    void testCannotApproveAvailableState() {
        assertThrows(IllegalStateException.class, () -> schedule.approve());
    }

    @Test
    void testCannotRejectAvailableState() {
        assertThrows(IllegalStateException.class, () -> schedule.reject());
    }

    @Test
    void testCannotRequestApprovedState() {
        schedule.request(patientId);
        schedule.approve();
        assertThrows(IllegalStateException.class, () -> schedule.request(patientId));
    }

    @Test
    void testCannotRequestRejectedState() {
        schedule.request(patientId);
        schedule.reject();
        assertThrows(IllegalStateException.class, () -> schedule.request(patientId));
    }

    @Test
    void testStatePersistsAfterSerialization() {
        schedule.request(patientId);
        Schedule newSchedule = Schedule.builder()
                .id(schedule.getId())
                .caregiverId(schedule.getCaregiverId())
                .patientId(schedule.getPatientId())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus())
                .build();
        newSchedule.setState(new RequestedState());
        assertEquals("REQUESTED", newSchedule.getStatus());
    }
}