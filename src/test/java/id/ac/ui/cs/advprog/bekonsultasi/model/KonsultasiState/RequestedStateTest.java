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

class RequestedStateTest {

    @Mock
    private Konsultasi konsultasi;

    private RequestedState requestedState;

    @BeforeEach
    void setUp() {
        openMocks(this);
        requestedState = new RequestedState();
    }

    @Test
    void testGetStateName() {
        assertEquals("REQUESTED", requestedState.getStateName());
    }

    @Test
    void testConfirm() {
        requestedState.confirm(konsultasi);
        verify(konsultasi).setState(any(ConfirmedState.class));
    }

    @Test
    void testCancel() {
        requestedState.cancel(konsultasi);
        verify(konsultasi).setState(any(CancelledState.class));
    }

    @Test
    void testComplete() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            requestedState.complete(konsultasi);
        });
        
        assertEquals("Cannot complete a consultation that's in requested state", exception.getMessage());
        verify(konsultasi, never()).setState(any());
    }

    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(5);
        requestedState.reschedule(konsultasi, newDateTime);
        verify(konsultasi).setScheduleDateTime(newDateTime);
    }
}