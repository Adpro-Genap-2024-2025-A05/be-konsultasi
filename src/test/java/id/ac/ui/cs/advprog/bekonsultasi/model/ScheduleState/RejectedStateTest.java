package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RejectedStateTest {
    private Schedule schedule;
    private RejectedState rejectedState;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        rejectedState = new RejectedState();
    }

    @Test
    void testGetStatusReturnsRejected() {
        assertEquals("REJECTED", rejectedState.getStatus());
    }

    @Test
    void testApproveThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                rejectedState.approve(schedule));
    }

    @Test
    void testRejectThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                rejectedState.reject(schedule));
    }

    @Test
    void testRequestThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                rejectedState.request(schedule, null));
    }
}