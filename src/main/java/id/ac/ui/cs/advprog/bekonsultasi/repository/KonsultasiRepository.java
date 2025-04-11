package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class KonsultasiRepository {
    private final Map<String, Konsultasi> konsultasiMap = new HashMap<>();

    public Konsultasi save(Konsultasi konsultasi) {
        if (konsultasi.getId() == null) {
            konsultasi.setId(UUID.randomUUID().toString());
        }
        konsultasiMap.put(konsultasi.getId(), konsultasi);
        return konsultasi;
    }

    public Konsultasi findById(String id) {
        return konsultasiMap.get(id);
    }

    public List<Konsultasi> findAll() {
        return new ArrayList<>(konsultasiMap.values());
    }

    public List<Konsultasi> findByPaciliansId(String paciliansId) {
        return findByPredicate(konsultasi ->
                Objects.equals(konsultasi.getPaciliansId(), paciliansId));
    }

    public List<Konsultasi> findByCareGiverId(String careGiverId) {
        return findByPredicate(konsultasi ->
                Objects.equals(konsultasi.getCareGiverId(), careGiverId));
    }

    private List<Konsultasi> findByPredicate(Predicate<Konsultasi> predicate) {
        return konsultasiMap.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        konsultasiMap.remove(id);
    }
}