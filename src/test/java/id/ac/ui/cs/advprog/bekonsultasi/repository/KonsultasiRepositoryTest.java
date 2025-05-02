package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
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
class KonsultasiRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KonsultasiRepository konsultasiRepository;

    @Test
    void testFindByPacilianId() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        
        Konsultasi konsultasi1 = Konsultasi.builder()
                .pacilianId(pacilianId)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes 1")
                .status("REQUESTED")
                .build();
        
        Konsultasi konsultasi2 = Konsultasi.builder()
                .pacilianId(pacilianId)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(10))
                .notes("Test notes 2")
                .status("CONFIRMED")
                .build();
        
        Konsultasi konsultasi3 = Konsultasi.builder()
                .pacilianId(UUID.randomUUID())
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(5))
                .notes("Test notes 3")
                .status("REQUESTED")
                .build();
        
        entityManager.persist(konsultasi1);
        entityManager.persist(konsultasi2);
        entityManager.persist(konsultasi3);
        entityManager.flush();
        
        List<Konsultasi> result = konsultasiRepository.findByPacilianId(pacilianId);
        
        assertEquals(2, result.size());
        assertEquals(pacilianId, result.get(0).getPacilianId());
        assertEquals(pacilianId, result.get(1).getPacilianId());
    }
    
    @Test
    void testFindByCaregiverId() {
        UUID pacilianId1 = UUID.randomUUID();
        UUID pacilianId2 = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        
        Konsultasi konsultasi1 = Konsultasi.builder()
                .pacilianId(pacilianId1)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes 1")
                .status("REQUESTED")
                .build();
        
        Konsultasi konsultasi2 = Konsultasi.builder()
                .pacilianId(pacilianId2)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(10))
                .notes("Test notes 2")
                .status("CONFIRMED")
                .build();
        
        Konsultasi konsultasi3 = Konsultasi.builder()
                .pacilianId(pacilianId1)
                .caregiverId(UUID.randomUUID())
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(5))
                .notes("Test notes 3")
                .status("REQUESTED")
                .build();
        
        entityManager.persist(konsultasi1);
        entityManager.persist(konsultasi2);
        entityManager.persist(konsultasi3);
        entityManager.flush();
        
        List<Konsultasi> result = konsultasiRepository.findByCaregiverId(caregiverId);
        
        assertEquals(2, result.size());
        assertEquals(caregiverId, result.get(0).getCaregiverId());
        assertEquals(caregiverId, result.get(1).getCaregiverId());
    }
    
    @Test
    void testFindByScheduleId() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID otherScheduleId = UUID.randomUUID();
        
        Konsultasi konsultasi1 = Konsultasi.builder()
                .pacilianId(pacilianId)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes 1")
                .status("REQUESTED")
                .build();
        
        Konsultasi konsultasi2 = Konsultasi.builder()
                .pacilianId(pacilianId)
                .caregiverId(caregiverId)
                .scheduleId(otherScheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(10))
                .notes("Test notes 2")
                .status("CONFIRMED")
                .build();
        
        entityManager.persist(konsultasi1);
        entityManager.persist(konsultasi2);
        entityManager.flush();
        
        List<Konsultasi> result = konsultasiRepository.findByScheduleId(scheduleId);
        
        assertEquals(1, result.size());
        assertEquals(scheduleId, result.get(0).getScheduleId());
    }
    
    @Test
    void testFindByStatusAndCaregiverId() {
        UUID pacilianId1 = UUID.randomUUID();
        UUID pacilianId2 = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        
        Konsultasi konsultasi1 = Konsultasi.builder()
                .pacilianId(pacilianId1)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes 1")
                .status("REQUESTED")
                .build();
        
        Konsultasi konsultasi2 = Konsultasi.builder()
                .pacilianId(pacilianId2)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(10))
                .notes("Test notes 2")
                .status("CONFIRMED")
                .build();
        
        Konsultasi konsultasi3 = Konsultasi.builder()
                .pacilianId(pacilianId1)
                .caregiverId(UUID.randomUUID())
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(5))
                .notes("Test notes 3")
                .status("REQUESTED")
                .build();
        
        entityManager.persist(konsultasi1);
        entityManager.persist(konsultasi2);
        entityManager.persist(konsultasi3);
        entityManager.flush();
        
        List<Konsultasi> result = konsultasiRepository.findByStatusAndCaregiverId("REQUESTED", caregiverId);
        
        assertEquals(1, result.size());
        assertEquals("REQUESTED", result.get(0).getStatus());
        assertEquals(caregiverId, result.get(0).getCaregiverId());
    }
    
    @Test
    void testSaveKonsultasi() {
        UUID pacilianId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        
        Konsultasi konsultasi = Konsultasi.builder()
                .pacilianId(pacilianId)
                .caregiverId(caregiverId)
                .scheduleId(scheduleId)
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes")
                .status("REQUESTED")
                .build();
        
        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
        
        assertNotNull(savedKonsultasi.getId());
        assertEquals(pacilianId, savedKonsultasi.getPacilianId());
        assertEquals(caregiverId, savedKonsultasi.getCaregiverId());
        assertEquals(scheduleId, savedKonsultasi.getScheduleId());
        assertEquals("REQUESTED", savedKonsultasi.getStatus());
    }
}