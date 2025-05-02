package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KonsultasiTest {

    private Konsultasi konsultasi;
    private UUID id;
    private UUID scheduleId;
    private UUID caregiverId;
    private UUID pacilianId;
    private LocalDateTime scheduleDateTime;
    private String notes;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        caregiverId = UUID.randomUUID();
        pacilianId = UUID.randomUUID();
        scheduleDateTime = LocalDateTime.now().plusDays(7);
        notes = "Test notes";
        
        konsultasi = Konsultasi.builder()
                .id(id)
                .scheduleId(scheduleId)
                .caregiverId(caregiverId)
                .pacilianId(pacilianId)
                .scheduleDateTime(scheduleDateTime)
                .notes(notes)
                .status("REQUESTED")
                .build();
        
        konsultasi.setState(new RequestedState());
    }

    @Test
    void testOnCreate() {
        Konsultasi newKonsultasi = new Konsultasi();
        newKonsultasi.onCreate();
        
        assertEquals("REQUESTED", newKonsultasi.getStatus());
        assertTrue(newKonsultasi.getState() instanceof RequestedState);
    }
    
    @Test
    void testSetState() {
        KonsultasiState newState = new ConfirmedState();
        konsultasi.setState(newState);
        
        assertEquals("CONFIRMED", konsultasi.getStatus());
        assertEquals(newState, konsultasi.getState());
    }
    
    @Test
    void testConfirm() {
        konsultasi.confirm();
        
        assertEquals("CONFIRMED", konsultasi.getStatus());
        assertTrue(konsultasi.getState() instanceof ConfirmedState);
    }
    
    @Test
    void testCancel() {
        konsultasi.cancel();
        
        assertEquals("CANCELLED", konsultasi.getStatus());
        assertTrue(konsultasi.getState() instanceof CancelledState);
    }
    
    @Test
    void testComplete() {
        // First confirm to move to a state where complete is allowed
        konsultasi.confirm();
        konsultasi.complete();
        
        assertEquals("DONE", konsultasi.getStatus());
        assertTrue(konsultasi.getState() instanceof DoneState);
    }
    
    @Test
    void testReschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(14);
        konsultasi.reschedule(newDateTime);
        
        assertEquals(newDateTime, konsultasi.getScheduleDateTime());
    }
    
    @Test
    void testIllegalStateTransition() {
        // Set to DONE state
        konsultasi.setState(new DoneState());
        
        // Attempt to confirm a completed consultation
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.confirm();
        });
        
        assertEquals("Cannot confirm a completed consultation", exception.getMessage());
    }
    
    @Test
    void testFullStateFlow() {
        // Initial state is REQUESTED
        assertEquals("REQUESTED", konsultasi.getStatus());
        
        // Confirm the consultation
        konsultasi.confirm();
        assertEquals("CONFIRMED", konsultasi.getStatus());
        
        // Reschedule the consultation
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(10);
        konsultasi.reschedule(newDateTime);
        assertEquals(newDateTime, konsultasi.getScheduleDateTime());
        
        // Complete the consultation
        konsultasi.complete();
        assertEquals("DONE", konsultasi.getStatus());
        
        // Verify that further state changes are blocked
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.cancel();
        });
        assertEquals("Cannot cancel a completed consultation", exception.getMessage());
    }
}