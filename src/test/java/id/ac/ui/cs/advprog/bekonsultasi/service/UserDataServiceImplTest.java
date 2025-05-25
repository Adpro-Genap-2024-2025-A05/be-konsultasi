package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CaregiverPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.PacilianPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Speciality;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataServiceImplTest {

    @Mock private RestTemplate restTemplate;
    @Mock private Counter caregiverDataRequestCounter;
    @Mock private Counter pacilianDataRequestCounter;
    @Mock private Counter userDataFetchErrorCounter;
    @Mock private Counter userDataFallbackCounter;
    @Mock private ResponseEntity<ApiResponseDto<CaregiverPublicDto>> caregiverResponse;
    @Mock private ResponseEntity<ApiResponseDto<PacilianPublicDto>> pacilianResponse;
    @Mock private ApiResponseDto<CaregiverPublicDto> caregiverApiResponse;
    @Mock private ApiResponseDto<PacilianPublicDto> pacilianApiResponse;

    private UserDataServiceImpl userDataService;
    private UUID caregiverId;
    private UUID pacilianId;
    private CaregiverPublicDto caregiverData;
    private PacilianPublicDto pacilianData;

    @BeforeEach
    void setUp() {
        userDataService = new UserDataServiceImpl(
                restTemplate,
                caregiverDataRequestCounter,
                pacilianDataRequestCounter,
                userDataFetchErrorCounter,
                userDataFallbackCounter
        );

        caregiverId = UUID.randomUUID();
        pacilianId = UUID.randomUUID();

        caregiverData = CaregiverPublicDto.builder()
                .id(caregiverId.toString())
                .name("Dr. Test")
                .email("test@test.com")
                .speciality(Speciality.DOKTER_UMUM)
                .workAddress("Test Address")
                .phoneNumber("123456789")
                .build();

        pacilianData = PacilianPublicDto.builder()
                .id(pacilianId.toString())
                .name("Patient Test")
                .email("patient@test.com")
                .address("Patient Address")
                .phoneNumber("987654321")
                .medicalHistory("No history")
                .build();
    }

    @Test
    void getCaregiverByIdAsync_Success() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        CompletableFuture<CaregiverPublicDto> result = userDataService.getCaregiverByIdAsync(caregiverId);

        assertNotNull(result);
        assertEquals(caregiverData, result.get());
    }

    @Test
    void getCaregiverByIdAsync_WithCache() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        userDataService.getCaregiverByIdAsync(caregiverId).get();
        CompletableFuture<CaregiverPublicDto> cachedResult = userDataService.getCaregiverByIdAsync(caregiverId);

        assertEquals(caregiverData, cachedResult.get());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void getCaregiverByIdAsync_ServiceError() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Service error"));

        CompletableFuture<CaregiverPublicDto> result = userDataService.getCaregiverByIdAsync(caregiverId);

        CaregiverPublicDto fallback = result.get();
        assertNotNull(fallback);
        assertEquals("Dr. [Data tidak tersedia]", fallback.getName());
    }

    @Test
    void getCaregiverByIdAsync_InvalidResponse() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(null);

        CompletableFuture<CaregiverPublicDto> result = userDataService.getCaregiverByIdAsync(caregiverId);

        CaregiverPublicDto fallback = result.get();
        assertNotNull(fallback);
        assertEquals("Dr. [Data tidak tersedia]", fallback.getName());
    }

    @Test
    void getCaregiverByIdAsync_NullData() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(null);

        CompletableFuture<CaregiverPublicDto> result = userDataService.getCaregiverByIdAsync(caregiverId);

        CaregiverPublicDto fallback = result.get();
        assertNotNull(fallback);
        assertEquals("Dr. [Data tidak tersedia]", fallback.getName());
    }

    @Test
    void getPacilianByIdAsync_Success() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(pacilianResponse);
        when(pacilianResponse.getBody()).thenReturn(pacilianApiResponse);
        when(pacilianApiResponse.getData()).thenReturn(pacilianData);

        CompletableFuture<PacilianPublicDto> result = userDataService.getPacilianByIdAsync(pacilianId);

        assertNotNull(result);
        assertEquals(pacilianData, result.get());
    }

    @Test
    void getPacilianByIdAsync_WithCache() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(pacilianResponse);
        when(pacilianResponse.getBody()).thenReturn(pacilianApiResponse);
        when(pacilianApiResponse.getData()).thenReturn(pacilianData);

        userDataService.getPacilianByIdAsync(pacilianId).get();
        CompletableFuture<PacilianPublicDto> cachedResult = userDataService.getPacilianByIdAsync(pacilianId);

        assertEquals(pacilianData, cachedResult.get());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void getPacilianByIdAsync_ServiceError() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Service error"));

        CompletableFuture<PacilianPublicDto> result = userDataService.getPacilianByIdAsync(pacilianId);

        PacilianPublicDto fallback = result.get();
        assertNotNull(fallback);
        assertEquals("[Nama tidak tersedia]", fallback.getName());
    }

    @Test
    void getPacilianByIdAsync_InvalidResponse() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(pacilianResponse);
        when(pacilianResponse.getBody()).thenReturn(null);

        CompletableFuture<PacilianPublicDto> result = userDataService.getPacilianByIdAsync(pacilianId);

        PacilianPublicDto fallback = result.get();
        assertNotNull(fallback);
        assertEquals("[Nama tidak tersedia]", fallback.getName());
    }

    @Test
    void getCaregiverById_Success() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        CaregiverPublicDto result = userDataService.getCaregiverById(caregiverId);

        assertNotNull(result);
        assertEquals(caregiverData.getName(), result.getName());
    }

    @Test
    void getCaregiverById_Exception() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Service error"));

        CaregiverPublicDto result = userDataService.getCaregiverById(caregiverId);

        assertNotNull(result);
        assertEquals("Dr. [Data tidak tersedia]", result.getName());
    }

    @Test
    void getPacilianById_Success() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(pacilianResponse);
        when(pacilianResponse.getBody()).thenReturn(pacilianApiResponse);
        when(pacilianApiResponse.getData()).thenReturn(pacilianData);

        PacilianPublicDto result = userDataService.getPacilianById(pacilianId);

        assertNotNull(result);
        assertEquals(pacilianData.getName(), result.getName());
    }

    @Test
    void getPacilianById_Exception() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Service error"));

        PacilianPublicDto result = userDataService.getPacilianById(pacilianId);

        assertNotNull(result);
        assertEquals("[Nama tidak tersedia]", result.getName());
    }

    @Test
    void clearCache_Success() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        userDataService.getCaregiverById(caregiverId);
        userDataService.clearCache();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        userDataService.getCaregiverById(caregiverId);

        verify(restTemplate, times(2)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void clearExpiredCache_Success() {
        userDataService.clearExpiredCache();
    }

    @Test
    void cacheExpiry_CaregiverCache() throws InterruptedException, ExecutionException {
        UserDataServiceImpl serviceWithShortTTL = spy(userDataService);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        CompletableFuture<CaregiverPublicDto> firstCall = serviceWithShortTTL.getCaregiverByIdAsync(caregiverId);
        firstCall.get();

        Thread.sleep(10);

        CompletableFuture<CaregiverPublicDto> secondCall = serviceWithShortTTL.getCaregiverByIdAsync(caregiverId);
        secondCall.get();
    }

    @Test
    void buildUrls_Correct() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(contains("/data/caregiver/" + caregiverId.toString()),
                any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        userDataService.getCaregiverByIdAsync(caregiverId).get();

        verify(restTemplate).exchange(contains("/data/caregiver/" + caregiverId.toString()),
                any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void fallbackData_CorrectFields() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Service error"));

        CaregiverPublicDto caregiverFallback = userDataService.getCaregiverById(caregiverId);
        PacilianPublicDto pacilianFallback = userDataService.getPacilianById(pacilianId);

        assertEquals(caregiverId.toString(), caregiverFallback.getId());
        assertEquals("Dr. [Data tidak tersedia]", caregiverFallback.getName());
        assertEquals("[Email tidak tersedia]", caregiverFallback.getEmail());
        assertEquals(Speciality.DOKTER_UMUM, caregiverFallback.getSpeciality());
        assertEquals("[Alamat tidak tersedia]", caregiverFallback.getWorkAddress());
        assertEquals("[Telepon tidak tersedia]", caregiverFallback.getPhoneNumber());

        assertEquals(pacilianId.toString(), pacilianFallback.getId());
        assertEquals("[Nama tidak tersedia]", pacilianFallback.getName());
        assertEquals("[Email tidak tersedia]", pacilianFallback.getEmail());
        assertEquals("[Alamat tidak tersedia]", pacilianFallback.getAddress());
        assertEquals("[Telepon tidak tersedia]", pacilianFallback.getPhoneNumber());
        assertEquals("[Riwayat Medis tidak tersedia]", pacilianFallback.getMedicalHistory());
    }

    @Test
    void httpEntity_CreatedCorrectly() throws ExecutionException, InterruptedException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(caregiverResponse);
        when(caregiverResponse.getBody()).thenReturn(caregiverApiResponse);
        when(caregiverApiResponse.getData()).thenReturn(caregiverData);

        userDataService.getCaregiverByIdAsync(caregiverId).get();

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class),
                argThat(entity -> {
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    return httpEntity.getHeaders().get("Content-Type").contains("application/json") &&
                            httpEntity.getHeaders().get("Accept").contains("application/json");
                }), any(ParameterizedTypeReference.class));
    }
}