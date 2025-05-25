package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KonsultasiTest {

    private Konsultasi konsultasi;
    private TestDataBuilder dataBuilder;

    @BeforeEach
    void setUp() {
        dataBuilder = new TestDataBuilder();
        konsultasi = dataBuilder.buildDefaultKonsultasi();
    }

    @Nested
    class ConstructionTests {
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
    }

    @Nested
    class StateTransitionTests {
        @Test
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
        void testComplete() {
            konsultasi.confirm();
            konsultasi.complete();

            assertEquals("DONE", konsultasi.getStatus());
            assertTrue(konsultasi.getState() instanceof DoneState);
        }

        @Test
        void testIllegalStateTransition() {
            konsultasi.setState(new DoneState());

            Exception exception = assertThrows(IllegalStateException.class, () -> {
                konsultasi.confirm();
            });

            assertEquals("Cannot confirm a completed consultation", exception.getMessage());
        }
    }

    @Nested
    class FunctionalityTests {
        @Test
        void testReschedule() {
            LocalDateTime newDateTime = dataBuilder.getDefaultScheduleDateTime().plusDays(7);
            konsultasi.reschedule(newDateTime);

            assertEquals(newDateTime, konsultasi.getScheduleDateTime());
        }
    }

    @Nested
    class IntegrationTests {
        @Test
        void testFullStateFlow() {
            assertEquals("REQUESTED", konsultasi.getStatus());

            konsultasi.confirm();
            assertEquals("CONFIRMED", konsultasi.getStatus());

            LocalDateTime newDateTime = dataBuilder.getDefaultScheduleDateTime().plusDays(3);
            konsultasi.reschedule(newDateTime);
            assertEquals("RESCHEDULED", konsultasi.getStatus());
            assertEquals(newDateTime, konsultasi.getScheduleDateTime());

            konsultasi.confirm();
            assertEquals("CONFIRMED", konsultasi.getStatus());

            konsultasi.complete();
            assertEquals("DONE", konsultasi.getStatus());

            Exception exception = assertThrows(IllegalStateException.class, () -> {
                konsultasi.cancel();
            });
            assertEquals("Cannot cancel a completed consultation", exception.getMessage());
        }
    }

    @Test
    void testRescheduleStoresOriginalDate() {
        LocalDateTime originalDateTime = LocalDateTime.now().plusDays(5);
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);

        konsultasi.setScheduleDateTime(originalDateTime);
        konsultasi.setState(new RequestedState());

        konsultasi.reschedule(newDateTime);

        assertEquals(originalDateTime, konsultasi.getOriginalScheduleDateTime());
        assertEquals(newDateTime, konsultasi.getScheduleDateTime());
        assertEquals("RESCHEDULED", konsultasi.getStatus());
    }

    @Test
    void testRevertToOriginalDateWhenRejected() {
        LocalDateTime originalDateTime = LocalDateTime.now().plusDays(5);
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);

        konsultasi.setScheduleDateTime(originalDateTime);
        konsultasi.setState(new RequestedState());

        konsultasi.reschedule(newDateTime);
        assertEquals("RESCHEDULED", konsultasi.getStatus());

        RescheduledState rescheduledState = (RescheduledState) konsultasi.getState();
        rescheduledState.reject(konsultasi);

        assertEquals(originalDateTime, konsultasi.getScheduleDateTime());
        assertEquals("CONFIRMED", konsultasi.getStatus());
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

    @Test
    void testOnUpdate_WhenLastUpdatedIsNull() {
        konsultasi.setLastUpdated(null);

        konsultasi.onUpdate();

        assertNotNull(konsultasi.getLastUpdated());
        assertTrue(konsultasi.getLastUpdated().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(konsultasi.getLastUpdated().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}