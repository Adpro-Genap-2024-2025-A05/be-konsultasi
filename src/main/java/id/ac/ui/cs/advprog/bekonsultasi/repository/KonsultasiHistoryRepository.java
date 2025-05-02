package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KonsultasiHistoryRepository extends JpaRepository<KonsultasiHistory, UUID> {
    List<KonsultasiHistory> findByKonsultasiIdOrderByTimestampDesc(UUID konsultasiId);
}