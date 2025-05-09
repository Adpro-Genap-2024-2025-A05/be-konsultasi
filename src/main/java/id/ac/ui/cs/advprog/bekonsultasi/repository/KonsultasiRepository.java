package id.ac.ui.cs.advprog.bekonsultasi.repository;

import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KonsultasiRepository extends JpaRepository<Konsultasi, UUID> {
    List<Konsultasi> findByPacilianId(UUID pacilianId);
    List<Konsultasi> findByCaregiverId(UUID caregiverId);
    List<Konsultasi> findByScheduleId(UUID scheduleId);
    List<Konsultasi> findByStatusAndCaregiverId(String status, UUID caregiverId);
    List<Konsultasi> findByPacilianIdAndStatusNotIn(UUID pacilianId, List<String> excludeStatuses);
}