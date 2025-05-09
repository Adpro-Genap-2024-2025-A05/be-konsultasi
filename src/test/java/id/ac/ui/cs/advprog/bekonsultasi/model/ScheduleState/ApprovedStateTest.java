package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApprovedStateTest {
    private Schedule schedule;
    private ApprovedState approvedState;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        approvedState = new ApprovedState();
    }

    @Test
    void testGetStatusReturnsApproved() {
        assertEquals("APPROVED", approvedState.getStatus());
    }

    @Test
    void testApproveThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                approvedState.approve(schedule));
    }

    @Test
    void testRequestThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                approvedState.request(schedule));
    }
}