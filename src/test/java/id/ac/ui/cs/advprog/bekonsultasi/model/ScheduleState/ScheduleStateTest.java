package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class ScheduleStateTest {

    private AvailableState availableState;
    private UnavailableState unavailableState;

    @Mock
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        availableState = new AvailableState();
        unavailableState = new UnavailableState();
    }

    @Test
    void testAvailableStateTransitions() {
        assertEquals("AVAILABLE", availableState.getStatus());
        availableState.makeUnavailable(schedule);
        verify(schedule).setState(org.mockito.ArgumentMatchers.any(UnavailableState.class));
    }

    @Test
    void testUnavailableStateTransitions() {
        assertEquals("UNAVAILABLE", unavailableState.getStatus());
        unavailableState.makeAvailable(schedule);
        verify(schedule).setState(org.mockito.ArgumentMatchers.any(AvailableState.class));
    }
}