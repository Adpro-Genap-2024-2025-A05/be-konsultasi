package id.ac.ui.cs.advprog.bekonsultasi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KonsultasiResponseDto {
    private UUID id;
    private UUID scheduleId;
    private UUID caregiverId;
    private UUID pacilianId;
    private LocalDateTime scheduleDateTime;
    private String notes;
    private String status;
    private LocalDateTime lastUpdated;

    private CaregiverPublicDto caregiverData;
    private PacilianPublicDto pacilianData;
}