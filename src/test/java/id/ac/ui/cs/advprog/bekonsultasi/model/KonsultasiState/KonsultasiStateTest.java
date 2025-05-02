package id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class KonsultasiStateTest {

    @Mock
    private Konsultasi konsultasi;

    private RequestedState requestedState;
    private ConfirmedState confirmedState;
    private CancelledState cancelledState;
    private DoneState doneState;
    private LocalDateTime scheduleDateTime;
    private LocalDateTime pastDateTime;

    @BeforeEach
    void setUp() {
        openMocks(this);
        requestedState = new RequestedState();
        confirmedState = new ConfirmedState();
        cancelledState = new CancelledState();
        doneState = new DoneState();
        
        scheduleDateTime = LocalDateTime.now().plusHours(48);
        pastDateTime = LocalDateTime.now().minusHours(24);
        
        when(konsultasi.getScheduleDateTime()).thenReturn(scheduleDateTime);
    }

    @Test
    void testRequestedStateGetStateName() {
        assertEquals("REQUESTED", requestedState.getStateName());
    }

    @Test
    void testRequestedStateConfirm() {
        requestedState.confirm(konsultasi);
        verify(konsultasi).setState(any(ConfirmedState.class));
    }
    
    @Test
    void testRequestedStateCancel() {
        requestedState.cancel(konsultasi);
        verify(konsultasi).setState(any(CancelledState.class));
    }
    
    @Test
    void testRequestedStateComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            requestedState.complete(konsultasi);
        });
        assertEquals("Cannot complete a consultation that's in requested state", exception.getMessage());
    }
    
    @Test
    void testRequestedStateReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        requestedState.reschedule(konsultasi, newDateTime);
        verify(konsultasi).setScheduleDateTime(newDateTime);
    }
    
    @Test
    void testConfirmedStateGetStateName() {
        assertEquals("CONFIRMED", confirmedState.getStateName());
    }
    
    @Test
    void testConfirmedStateConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            confirmedState.confirm(konsultasi);
        });
        assertEquals("Consultation is already confirmed", exception.getMessage());
    }
    
    @Test
    void testConfirmedStateCancelAllowed() {
        confirmedState.cancel(konsultasi);
        verify(konsultasi).setState(any(CancelledState.class));
    }
    
    @Test
    void testConfirmedStateCancelNotAllowed() {
        when(konsultasi.getScheduleDateTime()).thenReturn(LocalDateTime.now().plusHours(12));
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            confirmedState.cancel(konsultasi);
        });
        assertEquals("Cannot cancel a consultation less than 24 hours before the scheduled time", exception.getMessage());
    }
    
    @Test
    void testConfirmedStateComplete() {
        confirmedState.complete(konsultasi);
        verify(konsultasi).setState(any(DoneState.class));
    }
    
    @Test
    void testConfirmedStateReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        confirmedState.reschedule(konsultasi, newDateTime);
        verify(konsultasi).setScheduleDateTime(newDateTime);
    }
    
    @Test
    void testCancelledStateGetStateName() {
        assertEquals("CANCELLED", cancelledState.getStateName());
    }
    
    @Test
    void testCancelledStateConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.confirm(konsultasi);
        });
        assertEquals("Cannot confirm a cancelled consultation", exception.getMessage());
    }
    
    @Test
    void testCancelledStateCancel() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.cancel(konsultasi);
        });
        assertEquals("Consultation is already cancelled", exception.getMessage());
    }
    
    @Test
    void testCancelledStateComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.complete(konsultasi);
        });
        assertEquals("Cannot complete a cancelled consultation", exception.getMessage());
    }
    
    @Test
    void testCancelledStateReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.reschedule(konsultasi, newDateTime);
        });
        assertEquals("Cannot reschedule a cancelled consultation", exception.getMessage());
    }
    
    @Test
    void testDoneStateGetStateName() {
        assertEquals("DONE", doneState.getStateName());
    }
    
    @Test
    void testDoneStateConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.confirm(konsultasi);
        });
        assertEquals("Cannot confirm a completed consultation", exception.getMessage());
    }
    
    @Test
    void testDoneStateCancel() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.cancel(konsultasi);
        });
        assertEquals("Cannot cancel a completed consultation", exception.getMessage());
    }
    
    @Test
    void testDoneStateComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.complete(konsultasi);
        });
        assertEquals("Consultation is already completed", exception.getMessage());
    }
    
    @Test
    void testDoneStateReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.reschedule(konsultasi, newDateTime);
        });
        assertEquals("Cannot reschedule a completed consultation", exception.getMessage());
    }
}