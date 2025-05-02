package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ConfirmedStateTest {

    @Mock
    private Konsultasi konsultasi;

    private ConfirmedState confirmedState;
    private LocalDateTime futureDateTime;
    private LocalDateTime nearFutureDateTime;

    @BeforeEach
    void setUp() {
        openMocks(this);
        confirmedState = new ConfirmedState();
        
        futureDateTime = LocalDateTime.now().plusHours(48);
        nearFutureDateTime = LocalDateTime.now().plusHours(12);
    }

    @Test
    void testGetStateName() {
        assertEquals("CONFIRMED", confirmedState.getStateName());
    }

    @Test
    void testConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            confirmedState.confirm(konsultasi);
        });
        
        assertEquals("Consultation is already confirmed", exception.getMessage());
    }
    
    @Test
    void testCancelAllowed() {
        when(konsultasi.getScheduleDateTime()).thenReturn(futureDateTime);
        
        confirmedState.cancel(konsultasi);
        
        verify(konsultasi).setState(any(CancelledState.class));
    }
    
    @Test
    void testCancelNotAllowed() {
        when(konsultasi.getScheduleDateTime()).thenReturn(nearFutureDateTime);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            confirmedState.cancel(konsultasi);
        });
        
        assertEquals("Cannot cancel a consultation less than 24 hours before the scheduled time", exception.getMessage());
    }
    
    @Test
    void testComplete() {
        confirmedState.complete(konsultasi);
        
        verify(konsultasi).setState(any(DoneState.class));
    }
    
    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        confirmedState.reschedule(konsultasi, newDateTime);
        
        verify(konsultasi).setScheduleDateTime(newDateTime);
    }
}