package id.ac.ui.cs.advprog.bekonsultasi.dto;

import id.ac.ui.cs.advprog.bekonsultasi.enums.Speciality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaregiverPublicDto {
    private String id;
    private String name;
    private String email;
    private Speciality speciality;
    private String workAddress;
    private String phoneNumber;
}