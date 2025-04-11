package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import java.time.LocalDateTime;
import java.util.List;

public interface KonsultasiService {
    Konsultasi createKonsultasi(String paciliansId, String careGiverId, LocalDateTime schedule, String notes);
    Konsultasi getKonsultasiById(String id);
    List<Konsultasi> getAllKonsultasi();
    List<Konsultasi> getKonsultasiByPacilians(String paciliansId);
    List<Konsultasi> getKonsultasiByCareGiver(String careGiverId);

    Konsultasi confirmKonsultasi(String id);
    Konsultasi cancelKonsultasi(String id);
    Konsultasi completeKonsultasi(String id);
    Konsultasi rescheduleKonsultasi(String id, LocalDateTime newSchedule);

    List<KonsultasiHistory> getKonsultasiHistory(String id);
    void deleteKonsultasi(String id);
}