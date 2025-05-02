package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestedStateTest {
    private Schedule schedule;
    private RequestedState requestedState;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        requestedState = new RequestedState();
    }

    @Test
    void testGetStatusReturnsRequested() {
        assertEquals("REQUESTED", requestedState.getStatus());
    }

    @Test
    void testApproveChangesStateToApproved() {
        requestedState.approve(schedule);
        assertTrue(schedule.getState() instanceof ApprovedState);
    }

    @Test
    void testRejectChangesStateToRejected() {
        requestedState.reject(schedule);
        assertTrue(schedule.getState() instanceof RejectedState);
    }

    @Test
    void testRequestThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                requestedState.request(schedule));
    }
}