package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CaregiverPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.PacilianPublicDto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserDataService {
    CompletableFuture<CaregiverPublicDto> getCaregiverByIdAsync(UUID caregiverId);
    CompletableFuture<PacilianPublicDto> getPacilianByIdAsync(UUID pacilianId);
    CaregiverPublicDto getCaregiverById(UUID caregiverId);
    PacilianPublicDto getPacilianById(UUID pacilianId);
}