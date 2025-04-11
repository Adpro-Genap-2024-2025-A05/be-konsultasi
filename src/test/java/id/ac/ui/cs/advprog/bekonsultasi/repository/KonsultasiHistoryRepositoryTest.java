package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KonsultasiHistoryRepositoryTest {

    private KonsultasiHistoryRepository historyRepository;
    private KonsultasiHistory history1;
    private KonsultasiHistory history2;
    private Konsultasi konsultasi1;
    private Konsultasi konsultasi2;

    @BeforeEach
    public void setUp() {
        historyRepository = new KonsultasiHistoryRepository();

        LocalDateTime schedule = LocalDateTime.now().plusDays(1);

        konsultasi1 = new Konsultasi("P001", "C001", schedule, "Konsultasi 1");
        konsultasi1.setId("K001");

        konsultasi2 = new Konsultasi("P002", "C002", schedule, "Konsultasi 2");
        konsultasi2.setId("K002");

        history1 = new KonsultasiHistory(konsultasi1, "History 1");
        history2 = new KonsultasiHistory(konsultasi1, "History 2");
    }

    @Test
    public void testSaveNewHistory() {
        KonsultasiHistory savedHistory = historyRepository.save(history1);

        assertNotNull(savedHistory.getId());

        KonsultasiHistory retrievedHistory = historyRepository.findById(savedHistory.getId());
        assertNotNull(retrievedHistory);
        assertEquals(savedHistory.getId(), retrievedHistory.getId());
        assertEquals("History 1", retrievedHistory.getDescription());
        assertEquals("K001", retrievedHistory.getKonsultasi().getId());
    }

    @Test
    public void testUpdateExistingHistory() {
        KonsultasiHistory savedHistory = historyRepository.save(history1);
        String id = savedHistory.getId();

        savedHistory.setDescription("Updated description");
        historyRepository.save(savedHistory);

        KonsultasiHistory retrievedHistory = historyRepository.findById(id);
        assertEquals("Updated description", retrievedHistory.getDescription());
        assertEquals(id, retrievedHistory.getId());
    }

    @Test
    public void testFindAll() {
        historyRepository.save(history1);
        historyRepository.save(history2);

        List<KonsultasiHistory> allHistory = historyRepository.findAll();

        assertEquals(2, allHistory.size());
    }

    @Test
    public void testFindByKonsultasiId() {
        historyRepository.save(history1);
        historyRepository.save(history2);

        KonsultasiHistory history3 = new KonsultasiHistory(konsultasi2, "History 3");
        historyRepository.save(history3);

        List<KonsultasiHistory> historyList1 = historyRepository.findByKonsultasiId("K001");
        List<KonsultasiHistory> historyList2 = historyRepository.findByKonsultasiId("K002");

        assertEquals(2, historyList1.size());
        assertEquals(1, historyList2.size());
        assertEquals("K001", historyList1.get(0).getKonsultasi().getId());
        assertEquals("K002", historyList2.get(0).getKonsultasi().getId());
    }

    @Test
    public void testFindByNonexistentId() {
        KonsultasiHistory retrievedHistory = historyRepository.findById("nonexistent");
        assertNull(retrievedHistory);
    }

    @Test
    public void testDeleteHistory() {
        KonsultasiHistory savedHistory = historyRepository.save(history1);
        String id = savedHistory.getId();

        historyRepository.delete(id);

        KonsultasiHistory retrievedHistory = historyRepository.findById(id);
        assertNull(retrievedHistory);
    }

    @Test
    public void testEmptyRepository() {
        List<KonsultasiHistory> allHistory = historyRepository.findAll();

        assertTrue(allHistory.isEmpty());
    }

    @Test
    public void testFindByKonsultasiIdNoMatch() {
        historyRepository.save(history1);

        List<KonsultasiHistory> historyList = historyRepository.findByKonsultasiId("nonexistent");

        assertTrue(historyList.isEmpty());
    }

    @Test
    public void testConcurrentModification() {
        historyRepository.save(history1);
        historyRepository.save(history2);
        List<KonsultasiHistory> allHistory = historyRepository.findAll();

        allHistory.clear();

        List<KonsultasiHistory> checkHistory = historyRepository.findAll();
        assertEquals(2, checkHistory.size());
    }
}