package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CaregiverPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.PacilianPublicDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Speciality;

import io.micrometer.core.instrument.Counter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataServiceImpl implements UserDataService {
    private final RestTemplate restTemplate;
    
    @Value("${services.auth.url:http://localhost:8080/api}")
    private String authServiceUrl;

    private final Counter caregiverDataRequestCounter;
    private final Counter pacilianDataRequestCounter;
    private final Counter userDataFetchErrorCounter;
    private final Counter userDataFallbackCounter;

    @Override
    @Async
    public CompletableFuture<CaregiverPublicDto> getCaregiverByIdAsync(UUID caregiverId) {
        try {
            CaregiverPublicDto caregiver = getCaregiverById(caregiverId);
            return CompletableFuture.completedFuture(caregiver);
        } catch (Exception e) {
            log.error("Failed to fetch caregiver data for ID: {}", caregiverId, e);
            return CompletableFuture.completedFuture(createDefaultCaregiver(caregiverId));
        }
    }

    @Override
    @Async
    public CompletableFuture<PacilianPublicDto> getPacilianByIdAsync(UUID pacilianId) {
        try {
            PacilianPublicDto pacilian = getPacilianById(pacilianId);
            return CompletableFuture.completedFuture(pacilian);
        } catch (Exception e) {
            log.error("Failed to fetch pacilian data for ID: {}", pacilianId, e);
            return CompletableFuture.completedFuture(createDefaultPacilian(pacilianId));
        }
    }

    @Override
    public CaregiverPublicDto getCaregiverById(UUID caregiverId) {
        caregiverDataRequestCounter.increment();
        
        try {
            String url = authServiceUrl + "/data/caregiver/" + caregiverId.toString();
            
            ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>() {}
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            
            log.warn("No caregiver data found for ID: {}", caregiverId);
            userDataFallbackCounter.increment();
            return createDefaultCaregiver(caregiverId);
            
        } catch (Exception e) {
            userDataFetchErrorCounter.increment();
            log.error("Failed to fetch caregiver data for ID: {}", caregiverId, e);
            userDataFallbackCounter.increment();
            return createDefaultCaregiver(caregiverId);
        }
    }

    @Override
    public PacilianPublicDto getPacilianById(UUID pacilianId) {
        pacilianDataRequestCounter.increment();
        
        try {
            String url = authServiceUrl + "/data/pacilian/" + pacilianId.toString();
            
            ResponseEntity<ApiResponseDto<PacilianPublicDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<PacilianPublicDto>>() {}
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            
            log.warn("No pacilian data found for ID: {}", pacilianId);
            userDataFallbackCounter.increment();
            return createDefaultPacilian(pacilianId);
            
        } catch (Exception e) {
            userDataFetchErrorCounter.increment();
            log.error("Failed to fetch pacilian data for ID: {}", pacilianId, e);
            userDataFallbackCounter.increment();
            return createDefaultPacilian(pacilianId);
        }
    }

    private CaregiverPublicDto createDefaultCaregiver(UUID caregiverId) {
        return CaregiverPublicDto.builder()
                .id(caregiverId.toString())
                .name("Dr. [Data tidak tersedia]")
                .email("[Email tidak tersedia]")
                .speciality(Speciality.DOKTER_UMUM)
                .workAddress("[Alamat tidak tersedia]")
                .phoneNumber("[Telepon tidak tersedia]")
                .build();
    }

    private PacilianPublicDto createDefaultPacilian(UUID pacilianId) {
        return PacilianPublicDto.builder()
                .id(pacilianId.toString())
                .name("[Nama tidak tersedia]")
                .email("[Email tidak tersedia]")
                .address("[Alamat tidak tersedia]")
                .phoneNumber("[Telepon tidak tersedia]")
                .medicalHistory("[Riwayat Medis tidak tersedia]")
                .build();
    }
}