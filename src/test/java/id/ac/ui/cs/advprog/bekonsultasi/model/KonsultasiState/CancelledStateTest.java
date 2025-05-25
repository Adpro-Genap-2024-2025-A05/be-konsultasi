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
import static org.mockito.MockitoAnnotations.openMocks;

class CancelledStateTest {

    @Mock
    private Konsultasi konsultasi;

    private CancelledState cancelledState;

    @BeforeEach
    void setUp() {
        openMocks(this);
        cancelledState = new CancelledState();
    }

    @Test
    void testGetStateName() {
        assertEquals("CANCELLED", cancelledState.getStateName());
    }

    @Test
    void testConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.confirm(konsultasi);
        });
        
        assertEquals("Cannot confirm a cancelled consultation", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testCancel() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.cancel(konsultasi);
        });
        
        assertEquals("Consultation is already cancelled", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.complete(konsultasi);
        });
        
        assertEquals("Cannot complete a cancelled consultation", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cancelledState.reschedule(konsultasi, newDateTime);
        });
        
        assertEquals("Cannot reschedule a cancelled consultation", exception.getMessage());
        verify(konsultasi, never()).setScheduleDateTime(any());
    }
}