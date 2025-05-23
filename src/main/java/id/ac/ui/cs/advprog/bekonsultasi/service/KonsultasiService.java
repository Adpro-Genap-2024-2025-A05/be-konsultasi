package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;

import java.util.List;
import java.util.UUID;

public interface KonsultasiService {
    KonsultasiResponseDto createKonsultasi(CreateKonsultasiDto dto, UUID pacilianId);
    KonsultasiResponseDto confirmKonsultasi(UUID konsultasiId, UUID caregiverId);
    KonsultasiResponseDto cancelKonsultasi(UUID konsultasiId, UUID userId, String role);
    KonsultasiResponseDto completeKonsultasi(UUID konsultasiId, UUID caregiverId);
    KonsultasiResponseDto updateKonsultasiRequest(UUID konsultasiId, UpdateKonsultasiRequestDto dto, UUID pacilianId);
    KonsultasiResponseDto rescheduleKonsultasi(UUID konsultasiId, RescheduleKonsultasiDto dto, UUID caregiverId);
    KonsultasiResponseDto acceptReschedule(UUID konsultasiId, UUID pacilianId);
    KonsultasiResponseDto rejectReschedule(UUID konsultasiId, UUID pacilianId);
    KonsultasiResponseDto getKonsultasiById(UUID konsultasiId, UUID userId, String role);
    List<KonsultasiResponseDto> getKonsultasiByPacilianId(UUID pacilianId);
    List<KonsultasiResponseDto> getKonsultasiByCaregiverId(UUID caregiverId);
    List<KonsultasiResponseDto> getRequestedKonsultasiByCaregiverId(UUID caregiverId);
}