package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KonsultasiRepositoryTest {

    private KonsultasiRepository konsultasiRepository;
    private Konsultasi konsultasi1;
    private Konsultasi konsultasi2;
    private LocalDateTime schedule1;
    private LocalDateTime schedule2;

    @BeforeEach
    public void setUp() {
        konsultasiRepository = new KonsultasiRepository();

        schedule1 = LocalDateTime.now().plusDays(1);
        schedule2 = LocalDateTime.now().plusDays(2);

        konsultasi1 = new Konsultasi("P001", "C001", schedule1, "Konsultasi 1");
        konsultasi2 = new Konsultasi("P001", "C002", schedule2, "Konsultasi 2");
    }

    @Test
    public void testSaveNewKonsultasi() {
        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi1);
        assertNotNull(savedKonsultasi.getId());

        Konsultasi retrievedKonsultasi = konsultasiRepository.findById(savedKonsultasi.getId());
        assertNotNull(retrievedKonsultasi);
        assertEquals(savedKonsultasi.getId(), retrievedKonsultasi.getId());
        assertEquals("P001", retrievedKonsultasi.getPaciliansId());
        assertEquals("C001", retrievedKonsultasi.getCareGiverId());
        assertEquals(schedule1, retrievedKonsultasi.getSchedule());
        assertEquals("Konsultasi 1", retrievedKonsultasi.getNotes());
    }

    @Test
    public void testUpdateExistingKonsultasi() {
        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi1);
        String id = savedKonsultasi.getId();

        savedKonsultasi.setNotes("Updated notes");
        konsultasiRepository.save(savedKonsultasi);

        Konsultasi retrievedKonsultasi = konsultasiRepository.findById(id);
        assertEquals("Updated notes", retrievedKonsultasi.getNotes());
        assertEquals(id, retrievedKonsultasi.getId());
    }

    @Test
    public void testFindAll() {
        konsultasiRepository.save(konsultasi1);
        konsultasiRepository.save(konsultasi2);

        List<Konsultasi> allKonsultasi = konsultasiRepository.findAll();

        assertEquals(2, allKonsultasi.size());
    }

    @Test
    public void testFindByPaciliansId() {
        konsultasiRepository.save(konsultasi1);
        konsultasiRepository.save(konsultasi2);

        List<Konsultasi> konsultasiList = konsultasiRepository.findByPaciliansId("P001");

        assertEquals(2, konsultasiList.size());
    }

    @Test
    public void testFindByCareGiverId() {
        konsultasiRepository.save(konsultasi1);
        konsultasiRepository.save(konsultasi2);

        List<Konsultasi> konsultasiList1 = konsultasiRepository.findByCareGiverId("C001");
        List<Konsultasi> konsultasiList2 = konsultasiRepository.findByCareGiverId("C002");

        assertEquals(1, konsultasiList1.size());
        assertEquals(1, konsultasiList2.size());
        assertEquals("C001", konsultasiList1.get(0).getCareGiverId());
        assertEquals("C002", konsultasiList2.get(0).getCareGiverId());
    }

    @Test
    public void testFindByNonexistentId() {
        Konsultasi retrievedKonsultasi = konsultasiRepository.findById("nonexistent");
        assertNull(retrievedKonsultasi);
    }

    @Test
    public void testDeleteKonsultasi() {
        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi1);
        String id = savedKonsultasi.getId();
        konsultasiRepository.delete(id);
        Konsultasi retrievedKonsultasi = konsultasiRepository.findById(id);
        assertNull(retrievedKonsultasi);
    }

    @Test
    public void testEmptyRepository() {
        List<Konsultasi> allKonsultasi = konsultasiRepository.findAll();
        assertTrue(allKonsultasi.isEmpty());
    }

    @Test
    public void testFindByPaciliansIdNoMatch() {
        konsultasiRepository.save(konsultasi1);
        List<Konsultasi> konsultasiList = konsultasiRepository.findByPaciliansId("nonexistent");
        assertTrue(konsultasiList.isEmpty());
    }

    @Test
    public void testFindByCareGiverIdNoMatch() {
        konsultasiRepository.save(konsultasi1);
        List<Konsultasi> konsultasiList = konsultasiRepository.findByCareGiverId("nonexistent");
        assertTrue(konsultasiList.isEmpty());
    }
}