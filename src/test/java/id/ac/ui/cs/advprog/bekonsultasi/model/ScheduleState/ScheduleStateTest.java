package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleStateTest {
    @Test
    void testScheduleStateInterface() {
        ScheduleState state = new AvailableState();
        assertNotNull(state);
        assertEquals("AVAILABLE", state.getStatus());

        state = new RequestedState();
        assertEquals("REQUESTED", state.getStatus());

        state = new ApprovedState();
        assertEquals("APPROVED", state.getStatus());

    }
}