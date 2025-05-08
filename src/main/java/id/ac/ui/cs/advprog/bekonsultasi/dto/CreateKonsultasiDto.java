package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKonsultasiDto {
    
    @NotNull(message = "Schedule ID is required")
    private UUID scheduleId;
    
    private String notes;
}