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

class DoneStateTest {

    @Mock
    private Konsultasi konsultasi;

    private DoneState doneState;

    @BeforeEach
    void setUp() {
        openMocks(this);
        doneState = new DoneState();
    }

    @Test
    void testGetStateName() {
        assertEquals("DONE", doneState.getStateName());
    }

    @Test
    void testConfirm() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.confirm(konsultasi);
        });
        
        assertEquals("Cannot confirm a completed consultation", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testCancel() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.cancel(konsultasi);
        });
        
        assertEquals("Cannot cancel a completed consultation", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.complete(konsultasi);
        });
        
        assertEquals("Consultation is already completed", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }
    
    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            doneState.reschedule(konsultasi, newDateTime);
        });
        
        assertEquals("Cannot reschedule a completed consultation", exception.getMessage());
        verify(konsultasi, never()).setScheduleDateTime(any());
    }
}