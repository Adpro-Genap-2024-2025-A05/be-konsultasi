package id.ac.ui.cs.advprog.bekonsultasi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KonsultasiHistory Tests")
class KonsultasiHistoryTest {

    private KonsultasiHistory konsultasiHistory;
    private TestDataBuilder dataBuilder;
    private LocalDateTime beforeTimestamp;

    @BeforeEach
    void setUp() {
        beforeTimestamp = LocalDateTime.now();
        dataBuilder = new TestDataBuilder();
        konsultasiHistory = dataBuilder.buildDefaultKonsultasiHistory();
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {
        @Test
        void testOnCreate() {
            konsultasiHistory.onCreate();
            assertNotNull(konsultasiHistory.getTimestamp());
            
            LocalDateTime afterTimestamp = LocalDateTime.now();
            LocalDateTime historyTimestamp = konsultasiHistory.getTimestamp();
            
            assertTrue(historyTimestamp.isAfter(beforeTimestamp) || historyTimestamp.equals(beforeTimestamp));
            assertTrue(historyTimestamp.isBefore(afterTimestamp) || historyTimestamp.equals(afterTimestamp));
        }
        
        @Test
        void testOnCreateDoesNotOverride() {
            LocalDateTime existingTimestamp = LocalDateTime.now().minusDays(1);
            konsultasiHistory.setTimestamp(existingTimestamp);
            
            konsultasiHistory.onCreate();
            assertEquals(existingTimestamp, konsultasiHistory.getTimestamp());
        }
    }
    
    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {
        @Test
        void testGettersAndSetters() {
            UUID newId = UUID.randomUUID();
            UUID newKonsultasiId = UUID.randomUUID();
            UUID newModifiedBy = UUID.randomUUID();
            String newPreviousStatus = "CONFIRMED";
            String newNewStatus = "DONE";
            String newNotes = "Completed by caregiver";
            LocalDateTime newTimestamp = LocalDateTime.now().plusDays(1);
            
            konsultasiHistory.setId(newId);
            konsultasiHistory.setKonsultasiId(newKonsultasiId);
            konsultasiHistory.setModifiedBy(newModifiedBy);
            konsultasiHistory.setPreviousStatus(newPreviousStatus);
            konsultasiHistory.setNewStatus(newNewStatus);
            konsultasiHistory.setNotes(newNotes);
            konsultasiHistory.setTimestamp(newTimestamp);
            
            assertEquals(newId, konsultasiHistory.getId());
            assertEquals(newKonsultasiId, konsultasiHistory.getKonsultasiId());
            assertEquals(newModifiedBy, konsultasiHistory.getModifiedBy());
            assertEquals(newPreviousStatus, konsultasiHistory.getPreviousStatus());
            assertEquals(newNewStatus, konsultasiHistory.getNewStatus());
            assertEquals(newNotes, konsultasiHistory.getNotes());
            assertEquals(newTimestamp, konsultasiHistory.getTimestamp());
        }
    }
    
    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        @Test
        void testEqualsAndHashCode() {
            KonsultasiHistory sameHistory = dataBuilder.buildDefaultKonsultasiHistory();
            
            KonsultasiHistory differentHistory = KonsultasiHistory.builder()
                    .id(UUID.randomUUID())
                    .konsultasiId(dataBuilder.getKonsultasiId())
                    .previousStatus("REQUESTED")
                    .newStatus("CONFIRMED")
                    .modifiedBy(dataBuilder.getModifiedBy())
                    .notes("Confirmed by caregiver")
                    .build();
            
            assertEquals(konsultasiHistory, sameHistory);
            assertEquals(konsultasiHistory.hashCode(), sameHistory.hashCode());
            
            assertNotEquals(konsultasiHistory, differentHistory);
            assertNotEquals(konsultasiHistory.hashCode(), differentHistory.hashCode());
        }
    }
    
    private static class TestDataBuilder {
        private UUID id;
        private UUID konsultasiId;
        private UUID modifiedBy;
        
        public TestDataBuilder() {
            this.id = UUID.randomUUID();
            this.konsultasiId = UUID.randomUUID();
            this.modifiedBy = UUID.randomUUID();
        }
        
        public KonsultasiHistory buildDefaultKonsultasiHistory() {
            return KonsultasiHistory.builder()
                    .id(id)
                    .konsultasiId(konsultasiId)
                    .previousStatus("REQUESTED")
                    .newStatus("CONFIRMED")
                    .modifiedBy(modifiedBy)
                    .notes("Confirmed by caregiver")
                    .build();
        }
        
        public UUID getKonsultasiId() {
            return this.konsultasiId;
        }
        
        public UUID getModifiedBy() {
            return this.modifiedBy;
        }
    }
}