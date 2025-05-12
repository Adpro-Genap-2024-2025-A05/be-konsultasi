package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class UnavailableStateTest {

    UnavailableState unavailableState;

    @Mock
    Schedule schedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unavailableState = new UnavailableState();
    }

    @Test
    void testGetStatus() {
        assertEquals("UNAVAILABLE", unavailableState.getStatus());
    }

    @Test
    void testMakeAvailable() {
        unavailableState.makeAvailable(schedule);
        verify(schedule).setState(org.mockito.ArgumentMatchers.any(AvailableState.class));
    }

    @Test
    void testMakeUnavailable() {
        unavailableState.makeUnavailable(schedule);
    }
}