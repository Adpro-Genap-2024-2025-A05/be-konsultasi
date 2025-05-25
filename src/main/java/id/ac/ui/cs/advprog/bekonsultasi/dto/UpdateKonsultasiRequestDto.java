package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.constraints.NotNull;
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
public class UpdateKonsultasiRequestDto {
    @NotNull(message = "New schedule date and time is required")
    private LocalDateTime newScheduleDateTime;

    private UUID newScheduleId;
    
    private String notes;
}