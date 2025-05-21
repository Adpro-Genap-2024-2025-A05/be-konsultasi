package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOneTimeScheduleDto {
    @NotNull(message = "Specific date is required")
    private LocalDate specificDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}