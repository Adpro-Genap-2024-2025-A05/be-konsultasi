package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Konsultasi Tests")
class KonsultasiTest {

    private Konsultasi konsultasi;
    private TestDataBuilder dataBuilder;

    @BeforeEach
    void setUp() {
        dataBuilder = new TestDataBuilder();
        konsultasi = dataBuilder.buildDefaultKonsultasi();
    }

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        @Test
        @DisplayName("Should set requested state on creation")
        void testOnCreate() {
            Konsultasi newKonsultasi = new Konsultasi();
            newKonsultasi.onCreate();
            
            assertEquals("REQUESTED", newKonsultasi.getStatus());
            assertTrue(newKonsultasi.getState() instanceof RequestedState);
        }
        
        @Test
        @DisplayName("Should update state and status when state changes")
        void testSetState() {
            KonsultasiState newState = new ConfirmedState();
            konsultasi.setState(newState);
            
            assertEquals("CONFIRMED", konsultasi.getStatus());
            assertEquals(newState, konsultasi.getState());
        }
    }
    
    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {
        @Test
        @DisplayName("Should transition to CONFIRMED state")
        void testConfirm() {
            konsultasi.confirm();
            
            assertEquals("CONFIRMED", konsultasi.getStatus());
            assertTrue(konsultasi.getState() instanceof ConfirmedState);
        }
        
        @Test
        @DisplayName("Should transition to CANCELLED state")
        void testCancel() {
            konsultasi.cancel();
            
            assertEquals("CANCELLED", konsultasi.getStatus());
            assertTrue(konsultasi.getState() instanceof CancelledState);
        }
        
        @Test
        @DisplayName("Should transition to DONE state after confirmation")
        void testComplete() {
            konsultasi.confirm();
            konsultasi.complete();
            
            assertEquals("DONE", konsultasi.getStatus());
            assertTrue(konsultasi.getState() instanceof DoneState);
        }
        
        @Test
        @DisplayName("Should throw exception for illegal state transition")
        void testIllegalStateTransition() {
            konsultasi.setState(new DoneState());
            
            Exception exception = assertThrows(IllegalStateException.class, () -> {
                konsultasi.confirm();
            });
            
            assertEquals("Cannot confirm a completed consultation", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Functionality Tests")
    class FunctionalityTests {
        @Test
        @DisplayName("Should update schedule date time")
        void testReschedule() {
            LocalDateTime newDateTime = dataBuilder.getDefaultScheduleDateTime().plusDays(7);
            konsultasi.reschedule(newDateTime);
            
            assertEquals(newDateTime, konsultasi.getScheduleDateTime());
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        @Test
        @DisplayName("Should follow complete state transition flow")
        void testFullStateFlow() {
            assertEquals("REQUESTED", konsultasi.getStatus());
            
            konsultasi.confirm();
            assertEquals("CONFIRMED", konsultasi.getStatus());
            
            LocalDateTime newDateTime = dataBuilder.getDefaultScheduleDateTime().plusDays(3);
            konsultasi.reschedule(newDateTime);
            assertEquals(newDateTime, konsultasi.getScheduleDateTime());
            
            konsultasi.complete();
            assertEquals("DONE", konsultasi.getStatus());
            
            Exception exception = assertThrows(IllegalStateException.class, () -> {
                konsultasi.cancel();
            });
            assertEquals("Cannot cancel a completed consultation", exception.getMessage());
        }
    }
    
    private static class TestDataBuilder {
        private UUID id;
        private UUID scheduleId;
        private UUID caregiverId;
        private UUID pacilianId;
        private LocalDateTime scheduleDateTime;
        private String notes;
        
        public TestDataBuilder() {
            this.id = UUID.randomUUID();
            this.scheduleId = UUID.randomUUID();
            this.caregiverId = UUID.randomUUID();
            this.pacilianId = UUID.randomUUID();
            this.scheduleDateTime = LocalDateTime.now().plusDays(7);
            this.notes = "Test notes";
        }
        
        public Konsultasi buildDefaultKonsultasi() {
            Konsultasi konsultasi = Konsultasi.builder()
                    .id(id)
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes(notes)
                    .status("REQUESTED")
                    .build();
            
            konsultasi.setState(new RequestedState());
            return konsultasi;
        }
        
        public LocalDateTime getDefaultScheduleDateTime() {
            return this.scheduleDateTime;
        }
    }
}