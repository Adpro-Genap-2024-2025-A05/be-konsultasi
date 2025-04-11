package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public class KonsultasiRepository {
    private List<Konsultasi> konsultasiList = new ArrayList<>();

    public Konsultasi save(Konsultasi konsultasi) {
        if (konsultasi.getId() == null) {
            konsultasi.setId(UUID.randomUUID().toString());
            konsultasiList.add(konsultasi);
        } else {
            for (int i = 0; i < konsultasiList.size(); i++) {
                if (konsultasiList.get(i).getId().equals(konsultasi.getId())) {
                    konsultasiList.set(i, konsultasi);
                    break;
                }
            }
        }
        return konsultasi;
    }

    public Konsultasi findById(String id) {
        for (Konsultasi konsultasi : konsultasiList) {
            if (konsultasi.getId().equals(id)) {
                return konsultasi;
            }
        }
        return null;
    }

    public List<Konsultasi> findAll() {
        return new ArrayList<>(konsultasiList);
    }

    public List<Konsultasi> findByPaciliansId(String paciliansId) {
        List<Konsultasi> result = new ArrayList<>();
        for (Konsultasi konsultasi : konsultasiList) {
            if (konsultasi.getPaciliansId().equals(paciliansId)) {
                result.add(konsultasi);
            }
        }
        return result;
    }

    public List<Konsultasi> findByCareGiverId(String careGiverId) {
        List<Konsultasi> result = new ArrayList<>();
        for (Konsultasi konsultasi : konsultasiList) {
            if (konsultasi.getCareGiverId().equals(careGiverId)) {
                result.add(konsultasi);
            }
        }
        return result;
    }

    public void delete(String id) {
        Iterator<Konsultasi> iterator = konsultasiList.iterator();
        while (iterator.hasNext()) {
            Konsultasi konsultasi = iterator.next();
            if (konsultasi.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }
}