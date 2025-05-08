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
public class KonsultasiHistoryDto {
    private UUID id;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    private String modifiedByUserType;
    private String notes;
}