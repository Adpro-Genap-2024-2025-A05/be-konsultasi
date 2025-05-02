package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class KonsultasiHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KonsultasiHistoryRepository konsultasiHistoryRepository;

    @Test
    void testFindByKonsultasiIdOrderByTimestampDesc() {
        UUID konsultasiId = UUID.randomUUID();
        UUID otherKonsultasiId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        LocalDateTime now = LocalDateTime.now();
        
        KonsultasiHistory history1 = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .timestamp(now.minusHours(2))
                .modifiedBy(userId)
                .notes("Consultation requested")
                .build();
        
        KonsultasiHistory history2 = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(now.minusHours(1))
                .modifiedBy(userId)
                .notes("Consultation confirmed")
                .build();
        
        KonsultasiHistory history3 = KonsultasiHistory.builder()
                .konsultasiId(otherKonsultasiId)
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .timestamp(now)
                .modifiedBy(userId)
                .notes("Other consultation requested")
                .build();
        
        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.persist(history3);
        entityManager.flush();
        
        List<KonsultasiHistory> result = konsultasiHistoryRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId);
        
        assertEquals(2, result.size());
        assertEquals(konsultasiId, result.get(0).getKonsultasiId());
        assertEquals(konsultasiId, result.get(1).getKonsultasiId());
        
        assertEquals("CONFIRMED", result.get(0).getNewStatus());
        assertEquals("REQUESTED", result.get(1).getNewStatus());
    }
    
    @Test
    void testSaveKonsultasiHistory() {
        UUID konsultasiId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        KonsultasiHistory history = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(LocalDateTime.now())
                .modifiedBy(userId)
                .notes("Consultation confirmed by caregiver")
                .build();
        
        KonsultasiHistory savedHistory = konsultasiHistoryRepository.save(history);
        
        assertNotNull(savedHistory.getId());
        assertEquals(konsultasiId, savedHistory.getKonsultasiId());
        assertEquals("REQUESTED", savedHistory.getPreviousStatus());
        assertEquals("CONFIRMED", savedHistory.getNewStatus());
        assertEquals(userId, savedHistory.getModifiedBy());
        assertEquals("Consultation confirmed by caregiver", savedHistory.getNotes());
    }
    
    @Test
    void testOnCreateSetsTimestamp() {
        UUID konsultasiId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        KonsultasiHistory history = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .modifiedBy(userId)
                .notes("Consultation confirmed by caregiver")
                .build();
        
        history.onCreate();
        
        assertNotNull(history.getTimestamp());
    }
    
    @Test
    void testMultipleHistoryEntriesForSameKonsultation() {
        UUID konsultasiId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        
        LocalDateTime baseTime = LocalDateTime.now();
        
        KonsultasiHistory history1 = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .timestamp(baseTime.minusDays(3))
                .modifiedBy(userId)
                .notes("Consultation requested")
                .build();
        
        KonsultasiHistory history2 = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(baseTime.minusDays(2))
                .modifiedBy(caregiverId)
                .notes("Consultation confirmed")
                .build();
        
        KonsultasiHistory history3 = KonsultasiHistory.builder()
                .konsultasiId(konsultasiId)
                .previousStatus("CONFIRMED")
                .newStatus("DONE")
                .timestamp(baseTime.minusDays(1))
                .modifiedBy(caregiverId)
                .notes("Consultation completed")
                .build();
        
        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.persist(history3);
        entityManager.flush();
        
        List<KonsultasiHistory> result = konsultasiHistoryRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId);
        
        assertEquals(3, result.size());
        assertEquals("DONE", result.get(0).getNewStatus());
        assertEquals("CONFIRMED", result.get(1).getNewStatus());
        assertEquals("REQUESTED", result.get(2).getNewStatus());
    }
}