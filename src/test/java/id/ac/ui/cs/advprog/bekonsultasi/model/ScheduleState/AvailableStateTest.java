package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class AvailableStateTest {

    AvailableState availableState;

    @Mock
    Schedule schedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        availableState = new AvailableState();
    }

    @Test
    void testGetStatus() {
        assertEquals("AVAILABLE", availableState.getStatus());
    }

    @Test
    void testMakeAvailable() {
        availableState.makeAvailable(schedule);
    }

    @Test
    void testMakeUnavailable() {
        availableState.makeUnavailable(schedule);
        verify(schedule).setState(org.mockito.ArgumentMatchers.any(UnavailableState.class));
    }
}