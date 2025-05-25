package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class RescheduledStateTest {

    @Mock
    private Konsultasi konsultasi;

    private RescheduledState rescheduledState;
    private LocalDateTime originalDateTime;

    @BeforeEach
    void setUp() {
        openMocks(this);
        rescheduledState = new RescheduledState();
        originalDateTime = LocalDateTime.now().minusDays(2);
        when(konsultasi.getOriginalScheduleDateTime()).thenReturn(originalDateTime);
    }

    @Test
    void testGetStateName() {
        assertEquals("RESCHEDULED", rescheduledState.getStateName());
    }

    @Test
    void testConfirm() {
        rescheduledState.confirm(konsultasi);
        verify(konsultasi).setState(any(ConfirmedState.class));
    }

    @Test
    void testCancel() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            rescheduledState.cancel(konsultasi);
        });
        
        assertEquals("Cannot cancel a rescheduled consultation. It must be accepted or rejected.", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }

    @Test
    void testComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            rescheduledState.complete(konsultasi);
        });
        
        assertEquals("Cannot complete a consultation that is in rescheduled state", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }

    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        
        rescheduledState.reschedule(konsultasi, newDateTime);
        
        verify(konsultasi).setOriginalScheduleDateTime(originalDateTime);
        verify(konsultasi).setScheduleDateTime(newDateTime);
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testReject() {
        rescheduledState.reject(konsultasi);
        
        verify(konsultasi).setScheduleDateTime(originalDateTime);
        verify(konsultasi).setState(any(ConfirmedState.class));
    }
}