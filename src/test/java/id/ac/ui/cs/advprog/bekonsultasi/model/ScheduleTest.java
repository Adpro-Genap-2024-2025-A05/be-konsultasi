package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.ScheduleState;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.UnavailableState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ScheduleTest {

    private Schedule schedule;

    @Mock
    private ScheduleState availableState;

    @Mock
    private ScheduleState unavailableState;

    private final UUID caregiverId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(availableState.getStatus()).thenReturn("AVAILABLE");
        when(unavailableState.getStatus()).thenReturn("UNAVAILABLE");

        schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
    }

    @Test
    void testSetState() {
        schedule.setState(availableState);
        assertEquals("AVAILABLE", schedule.getStatus());

        schedule.setState(unavailableState);
        assertEquals("UNAVAILABLE", schedule.getStatus());
    }

    @Test
    void testGetStatus() {
        schedule.setState(availableState);
        assertEquals("AVAILABLE", schedule.getStatus());
    }

    @Test
    void testMakeAvailable() {
        schedule.setState(unavailableState);
        when(unavailableState.getStatus()).thenReturn("UNAVAILABLE");
        AvailableState realAvailableState = new AvailableState();
        when(unavailableState.getStatus()).thenReturn("UNAVAILABLE");
        schedule = new Schedule() {
            @Override
            public void setState(ScheduleState state) {
                super.setState(realAvailableState);
            }
        };
        schedule.setState(unavailableState);
        schedule.makeAvailable();
        assertEquals("AVAILABLE", schedule.getStatus());
    }

    @Test
    void testMakeUnavailable() {
        schedule.setState(availableState);
        UnavailableState realUnavailableState = new UnavailableState();
        when(availableState.getStatus()).thenReturn("AVAILABLE");
        schedule = new Schedule() {
            @Override
            public void setState(ScheduleState state) {
                super.setState(realUnavailableState);
            }
        };
        schedule.setState(availableState);
        schedule.makeUnavailable();
        assertEquals("UNAVAILABLE", schedule.getStatus());
    }

    @Test
    void testOnCreate() {
        Schedule newSchedule = new Schedule();
        newSchedule.onCreate();
        assertEquals("AVAILABLE", newSchedule.getStatus());
    }
}