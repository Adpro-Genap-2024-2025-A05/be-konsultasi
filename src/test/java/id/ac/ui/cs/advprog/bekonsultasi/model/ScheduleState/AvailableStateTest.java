package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AvailableStateTest {
    private Schedule schedule;
    private AvailableState availableState;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        availableState = new AvailableState();
    }

    @Test
    void testGetStatusReturnsAvailable() {
        assertEquals("AVAILABLE", availableState.getStatus());
    }

    @Test
    void testApproveChangesStateToApproved() {
        availableState.approve(schedule);
        assertTrue(schedule.getState() instanceof ApprovedState);
    }

    @Test
    void testRequestThrowsUnsupportedOperationException() {
        Exception exception = assertThrows(UnsupportedOperationException.class, () ->
                availableState.request(schedule));

        assertEquals("Request operation not supported in this version", exception.getMessage());
    }
}