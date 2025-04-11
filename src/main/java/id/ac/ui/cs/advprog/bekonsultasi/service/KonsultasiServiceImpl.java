package id.ac.ui.cs.advprog.bekonsultasi.service;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.RequestedState;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KonsultasiServiceImpl implements KonsultasiService {

    private final KonsultasiRepository konsultasiRepository;
    private final KonsultasiHistoryRepository historyRepository;

    @Autowired
    public KonsultasiServiceImpl(
            KonsultasiRepository konsultasiRepository,
            KonsultasiHistoryRepository historyRepository) {
        this.konsultasiRepository = konsultasiRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Konsultasi createKonsultasi(String paciliansId, String careGiverId, LocalDateTime schedule,
                                       String notes) {
        Konsultasi konsultasi = new Konsultasi(paciliansId, careGiverId, schedule, notes);

        konsultasi.setState(new RequestedState());
        konsultasi.addHistory("Permintaan konsultasi dibuat");

        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

        for (KonsultasiHistory history : savedKonsultasi.getHistoryList()) {
            historyRepository.save(history);
        }

        return savedKonsultasi;
    }

    @Override
    public Konsultasi getKonsultasiById(String id) {
        Konsultasi konsultasi = konsultasiRepository.findById(id);
        if (konsultasi == null) {
            throw new RuntimeException("Konsultasi tidak ditemukan");
        }
        return konsultasi;
    }

    @Override
    public List<Konsultasi> getAllKonsultasi() {
        return konsultasiRepository.findAll();
    }

    @Override
    public List<Konsultasi> getKonsultasiByPacilians(String paciliansId) {
        return konsultasiRepository.findByPaciliansId(paciliansId);
    }

    @Override
    public List<Konsultasi> getKonsultasiByCareGiver(String careGiverId) {
        return konsultasiRepository.findByCareGiverId(careGiverId);
    }

    @Override
    public Konsultasi confirmKonsultasi(String id) {
        Konsultasi konsultasi = getKonsultasiById(id);

        try {
            konsultasi.confirm();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            KonsultasiHistory latestHistory = savedKonsultasi.getHistoryList()
                    .get(savedKonsultasi.getHistoryList().size() - 1);
            historyRepository.save(latestHistory);

            return savedKonsultasi;
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Konsultasi cancelKonsultasi(String id) {
        Konsultasi konsultasi = getKonsultasiById(id);

        try {
            konsultasi.cancel();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            KonsultasiHistory latestHistory = savedKonsultasi.getHistoryList()
                    .get(savedKonsultasi.getHistoryList().size() - 1);
            historyRepository.save(latestHistory);

            return savedKonsultasi;
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Konsultasi completeKonsultasi(String id) {
        Konsultasi konsultasi = getKonsultasiById(id);

        try {
            konsultasi.complete();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            KonsultasiHistory latestHistory = savedKonsultasi.getHistoryList()
                    .get(savedKonsultasi.getHistoryList().size() - 1);
            historyRepository.save(latestHistory);

            return savedKonsultasi;
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Konsultasi rescheduleKonsultasi(String id, LocalDateTime newSchedule) {
        Konsultasi konsultasi = getKonsultasiById(id);

        try {
            konsultasi.reschedule(newSchedule);
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            KonsultasiHistory latestHistory = savedKonsultasi.getHistoryList()
                    .get(savedKonsultasi.getHistoryList().size() - 1);
            historyRepository.save(latestHistory);

            return savedKonsultasi;
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public List<KonsultasiHistory> getKonsultasiHistory(String id) {
        return historyRepository.findByKonsultasiId(id);
    }

    @Override
    public void deleteKonsultasi(String id) {
        List<KonsultasiHistory> histories = historyRepository.findByKonsultasiId(id);
        for (KonsultasiHistory history : histories) {
            historyRepository.delete(history.getId());
        }

        konsultasiRepository.delete(id);
    }
}