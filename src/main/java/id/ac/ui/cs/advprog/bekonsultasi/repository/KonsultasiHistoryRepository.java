package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public class KonsultasiHistoryRepository {
    private List<KonsultasiHistory> historyList = new ArrayList<>();

    public KonsultasiHistory save(KonsultasiHistory history) {
        if (history.getId() == null) {
            history.setId(UUID.randomUUID().toString());
            historyList.add(history);
        } else {
            for (int i = 0; i < historyList.size(); i++) {
                if (historyList.get(i).getId().equals(history.getId())) {
                    historyList.set(i, history);
                    break;
                }
            }
        }
        return history;
    }

    public KonsultasiHistory findById(String id) {
        for (KonsultasiHistory history : historyList) {
            if (history.getId().equals(id)) {
                return history;
            }
        }
        return null;
    }

    public List<KonsultasiHistory> findAll() {
        return new ArrayList<>(historyList);
    }

    public List<KonsultasiHistory> findByKonsultasiId(String konsultasiId) {
        List<KonsultasiHistory> result = new ArrayList<>();
        for (KonsultasiHistory history : historyList) {
            if (history.getKonsultasi().getId().equals(konsultasiId)) {
                result.add(history);
            }
        }
        return result;
    }

    public void delete(String id) {
        Iterator<KonsultasiHistory> iterator = historyList.iterator();
        while (iterator.hasNext()) {
            KonsultasiHistory history = iterator.next();
            if (history.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }
}