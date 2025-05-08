package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleKonsultasiDto {
    @NotNull(message = "New schedule date and time is required")
    private LocalDateTime newScheduleDateTime;
    
    private String notes;
}