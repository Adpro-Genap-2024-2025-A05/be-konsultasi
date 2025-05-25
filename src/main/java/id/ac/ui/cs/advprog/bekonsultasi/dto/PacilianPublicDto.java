package id.ac.ui.cs.advprog.bekonsultasi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacilianPublicDto {
    private String id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private String medicalHistory;
}