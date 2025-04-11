package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class KonsultasiHistoryRepository {
    private final Map<String, KonsultasiHistory> historyMap = new HashMap<>();

    public KonsultasiHistory save(KonsultasiHistory history) {
        if (history.getId() == null) {
            history.setId(UUID.randomUUID().toString());
        }
        historyMap.put(history.getId(), history);
        return history;
    }

    public KonsultasiHistory findById(String id) {
        return historyMap.get(id);
    }

    public List<KonsultasiHistory> findAll() {
        return new ArrayList<>(historyMap.values());
    }

    public List<KonsultasiHistory> findByKonsultasiId(String konsultasiId) {
        return findByPredicate(history ->
                Objects.equals(history.getKonsultasi().getId(), konsultasiId));
    }

    private List<KonsultasiHistory> findByPredicate(Predicate<KonsultasiHistory> predicate) {
        return historyMap.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        historyMap.remove(id);
    }
}