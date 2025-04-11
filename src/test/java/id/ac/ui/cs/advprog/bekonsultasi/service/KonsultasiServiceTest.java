package id.ac.ui.cs.advprog.bekonsultasi.service;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KonsultasiServiceTest {

    @Mock
    private KonsultasiRepository konsultasiRepository;

    @Mock
    private KonsultasiHistoryRepository historyRepository;

    @InjectMocks
    private KonsultasiServiceImpl konsultasiService;

    private Konsultasi konsultasi;
    private String paciliansId;
    private String careGiverId;
    private LocalDateTime schedule;
    private String notes;
    private String konsultasiId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        paciliansId = "P001";
        careGiverId = "C001";
        schedule = LocalDateTime.now().plusDays(2);
        notes = "Test konsultasi";
        konsultasiId = "K001";

        konsultasi = new Konsultasi(paciliansId, careGiverId, schedule, notes);
        konsultasi.setId(konsultasiId);

        when(konsultasiRepository.findById(konsultasiId)).thenReturn(konsultasi);
        when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testCreateKonsultasi() {
        Konsultasi createdKonsultasi = konsultasiService.createKonsultasi(
                paciliansId, careGiverId, schedule, notes);

        verify(konsultasiRepository, times(1)).save(any(Konsultasi.class));

        verify(historyRepository, atLeastOnce()).save(any(KonsultasiHistory.class));

        assertEquals(paciliansId, createdKonsultasi.getPaciliansId());
        assertEquals(careGiverId, createdKonsultasi.getCareGiverId());
        assertEquals(schedule, createdKonsultasi.getSchedule());
        assertEquals(notes, createdKonsultasi.getNotes());
        assertEquals("REQUESTED", createdKonsultasi.getStateValue());
    }

    @Test
    public void testGetKonsultasiById() {
        Konsultasi foundKonsultasi = konsultasiService.getKonsultasiById(konsultasiId);

        assertNotNull(foundKonsultasi);
        assertEquals(konsultasiId, foundKonsultasi.getId());

        verify(konsultasiRepository, times(1)).findById(konsultasiId);
    }

    @Test
    public void testGetKonsultasiByIdNotFound() {
        String nonExistentId = "nonexistent";
        when(konsultasiRepository.findById(nonExistentId)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            konsultasiService.getKonsultasiById(nonExistentId);
        });

        assertEquals("Konsultasi tidak ditemukan", exception.getMessage());
    }

    @Test
    public void testConfirmKonsultasi() {
        Konsultasi confirmedKonsultasi = konsultasiService.confirmKonsultasi(konsultasiId);
        assertEquals("CONFIRMED", confirmedKonsultasi.getStateValue());
        verify(konsultasiRepository, times(1)).save(any(Konsultasi.class));
        verify(historyRepository, times(1)).save(any(KonsultasiHistory.class));
    }

    @Test
    public void testCancelKonsultasi() {
        Konsultasi cancelledKonsultasi = konsultasiService.cancelKonsultasi(konsultasiId);
        assertEquals("CANCELLED", cancelledKonsultasi.getStateValue());
        verify(konsultasiRepository, times(1)).save(any(Konsultasi.class));
        verify(historyRepository, times(1)).save(any(KonsultasiHistory.class));
    }

    @Test
    public void testCompleteKonsultasi() {
        konsultasi.confirm();
        Konsultasi completedKonsultasi = konsultasiService.completeKonsultasi(konsultasiId);
        assertEquals("DONE", completedKonsultasi.getStateValue());
        verify(konsultasiRepository, times(1)).save(any(Konsultasi.class));
        verify(historyRepository, times(1)).save(any(KonsultasiHistory.class));
    }

    @Test
    public void testRescheduleKonsultasi() {
        LocalDateTime newSchedule = schedule.plusDays(3);
        Konsultasi rescheduledKonsultasi = konsultasiService.rescheduleKonsultasi(konsultasiId, newSchedule);
        assertEquals(newSchedule, rescheduledKonsultasi.getSchedule());
        verify(konsultasiRepository, times(1)).save(any(Konsultasi.class));
        verify(historyRepository, times(1)).save(any(KonsultasiHistory.class));
    }

    @Test
    public void testGetKonsultasiHistory() {
        List<KonsultasiHistory> historyList = new ArrayList<>();
        KonsultasiHistory history = new KonsultasiHistory(konsultasi, "Test history");
        historyList.add(history);

        when(historyRepository.findByKonsultasiId(konsultasiId)).thenReturn(historyList);
        List<KonsultasiHistory> retrievedHistory = konsultasiService.getKonsultasiHistory(konsultasiId);
        assertNotNull(retrievedHistory);
        assertEquals(1, retrievedHistory.size());
        verify(historyRepository, times(1)).findByKonsultasiId(konsultasiId);
    }

    @Test
    public void testDeleteKonsultasi() {
        List<KonsultasiHistory> historyList = new ArrayList<>();
        KonsultasiHistory history = new KonsultasiHistory(konsultasi, "Test history");
        history.setId("H001");
        historyList.add(history);

        when(historyRepository.findByKonsultasiId(konsultasiId)).thenReturn(historyList);
        konsultasiService.deleteKonsultasi(konsultasiId);
        verify(historyRepository, times(1)).delete("H001");
        verify(konsultasiRepository, times(1)).delete(konsultasiId);
    }
}