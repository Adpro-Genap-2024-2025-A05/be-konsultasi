package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CaregiverPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.PacilianPublicDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Speciality;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.instrument.Counter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
@Slf4j
public class UserDataServiceImpl implements UserDataService {
    private final RestTemplate restTemplate;
    
    @Value("${services.auth.url:http://localhost:8080/api}")
    private String authServiceUrl;

    private final Counter caregiverDataRequestCounter;
    private final Counter pacilianDataRequestCounter;
    private final Counter userDataFetchErrorCounter;
    private final Counter userDataFallbackCounter;
    
    private final ConcurrentHashMap<UUID, CacheEntry<CaregiverPublicDto>> caregiverCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CacheEntry<PacilianPublicDto>> pacilianCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration FALLBACK_CACHE_TTL = Duration.ofMinutes(1);
    
    private final HttpEntity<?> httpEntity;
    
    public UserDataServiceImpl(RestTemplate restTemplate, 
                              Counter caregiverDataRequestCounter,
                              Counter pacilianDataRequestCounter, 
                              Counter userDataFetchErrorCounter,
                              Counter userDataFallbackCounter) {
        this.restTemplate = restTemplate;
        this.caregiverDataRequestCounter = caregiverDataRequestCounter;
        this.pacilianDataRequestCounter = pacilianDataRequestCounter;
        this.userDataFetchErrorCounter = userDataFetchErrorCounter;
        this.userDataFallbackCounter = userDataFallbackCounter;
        this.httpEntity = createHttpEntity();
    }

    @Override
    @Async("userDataTaskExecutor")
    public CompletableFuture<CaregiverPublicDto> getCaregiverByIdAsync(UUID caregiverId) {
        log.info("Async fetching caregiver data for ID: {}", caregiverId);
        caregiverDataRequestCounter.increment();
        
        CaregiverPublicDto cachedResult = getCachedCaregiver(caregiverId);
        if (cachedResult != null) {
            return CompletableFuture.completedFuture(cachedResult);
        }
        
        return fetchCaregiverFromService(caregiverId);
    }

    @Override
    @Async("userDataTaskExecutor")
    public CompletableFuture<PacilianPublicDto> getPacilianByIdAsync(UUID pacilianId) {
        log.info("Async fetching pacilian data for ID: {}", pacilianId);
        pacilianDataRequestCounter.increment();
        
        PacilianPublicDto cachedResult = getCachedPacilian(pacilianId);
        if (cachedResult != null) {
            return CompletableFuture.completedFuture(cachedResult);
        }
        
        return fetchPacilianFromService(pacilianId);
    }


    @Override
    public CaregiverPublicDto getCaregiverById(UUID caregiverId) {
        log.info("Sync fetching caregiver data for ID: {}", caregiverId);
        try {
            return getCaregiverByIdAsync(caregiverId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted while fetching caregiver data for ID: {}", caregiverId);
            return handleCaregiverFetchError(caregiverId, e);
        } catch (Exception e) {
            return handleCaregiverFetchError(caregiverId, e);
        }
    }

    @Override
    public PacilianPublicDto getPacilianById(UUID pacilianId) {
        log.info("Sync fetching pacilian data for ID: {}", pacilianId);
        try {
            return getPacilianByIdAsync(pacilianId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted while fetching pacilian data for ID: {}", pacilianId);
            return handlePacilianFetchError(pacilianId, e);
        } catch (Exception e) {
            return handlePacilianFetchError(pacilianId, e);
        }
    }

    private CaregiverPublicDto getCachedCaregiver(UUID caregiverId) {
        CacheEntry<CaregiverPublicDto> cached = caregiverCache.get(caregiverId);
        return (cached != null && !cached.isExpired()) ? cached.getValue() : null;
    }

    private PacilianPublicDto getCachedPacilian(UUID pacilianId) {
        CacheEntry<PacilianPublicDto> cached = pacilianCache.get(pacilianId);
        return (cached != null && !cached.isExpired()) ? cached.getValue() : null;
    }

    private void cacheCaregiverData(UUID caregiverId, CaregiverPublicDto data) {
        cacheCaregiverData(caregiverId, data, CACHE_TTL);
    }

    private void cacheCaregiverData(UUID caregiverId, CaregiverPublicDto data, Duration ttl) {
        caregiverCache.put(caregiverId, new CacheEntry<>(data, LocalDateTime.now(), ttl));
    }

    private void cachePacilianData(UUID pacilianId, PacilianPublicDto data) {
        cachePacilianData(pacilianId, data, CACHE_TTL);
    }

    private void cachePacilianData(UUID pacilianId, PacilianPublicDto data, Duration ttl) {
        pacilianCache.put(pacilianId, new CacheEntry<>(data, LocalDateTime.now(), ttl));
    }

    private CompletableFuture<CaregiverPublicDto> fetchCaregiverFromService(UUID caregiverId) {
        try {
            String url = buildCaregiverUrl(caregiverId);
            
            ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response = executeRestCall(
                url, new ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>() {}
            );
            
            return processCaregiverResponse(caregiverId, response);
            
        } catch (Exception e) {
            return handleCaregiverServiceError(caregiverId, e);
        }
    }

    private CompletableFuture<PacilianPublicDto> fetchPacilianFromService(UUID pacilianId) {
        try {
            String url = buildPacilianUrl(pacilianId);
            
            ResponseEntity<ApiResponseDto<PacilianPublicDto>> response = executeRestCall(
                url, new ParameterizedTypeReference<ApiResponseDto<PacilianPublicDto>>() {}
            );
            
            return processPacilianResponse(pacilianId, response);
            
        } catch (Exception e) {
            return handlePacilianServiceError(pacilianId, e);
        }
    }

    private <T> ResponseEntity<ApiResponseDto<T>> executeRestCall(String url, ParameterizedTypeReference<ApiResponseDto<T>> typeReference) {
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, typeReference);
    }

    private String buildCaregiverUrl(UUID caregiverId) {
        return authServiceUrl + "/data/caregiver/" + caregiverId.toString();
    }

    private String buildPacilianUrl(UUID pacilianId) {
        return authServiceUrl + "/data/pacilian/" + pacilianId.toString();
    }

    private CompletableFuture<CaregiverPublicDto> processCaregiverResponse(UUID caregiverId, ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response) {
        if (isValidResponse(response)) {
            CaregiverPublicDto result = response.getBody().getData();
            cacheCaregiverData(caregiverId, result);
            log.info("Successfully fetched caregiver data for ID: {}", caregiverId);
            return CompletableFuture.completedFuture(result);
        }
        
        return createCaregiverFallbackResponse(caregiverId);
    }

    private CompletableFuture<PacilianPublicDto> processPacilianResponse(UUID pacilianId, ResponseEntity<ApiResponseDto<PacilianPublicDto>> response) {
        if (isValidResponse(response)) {
            PacilianPublicDto result = response.getBody().getData();
            cachePacilianData(pacilianId, result);
            log.info("Successfully fetched pacilian data for ID: {}", pacilianId);
            return CompletableFuture.completedFuture(result);
        }
        
        return createPacilianFallbackResponse(pacilianId);
    }

    private <T> boolean isValidResponse(ResponseEntity<ApiResponseDto<T>> response) {
        return response.getBody() != null && response.getBody().getData() != null;
    }

    private CompletableFuture<CaregiverPublicDto> createCaregiverFallbackResponse(UUID caregiverId) {
        recordFallbackMetrics();
        CaregiverPublicDto fallback = createDefaultCaregiver(caregiverId);
        cacheCaregiverData(caregiverId, fallback, FALLBACK_CACHE_TTL);
        return CompletableFuture.completedFuture(fallback);
    }

    private CompletableFuture<PacilianPublicDto> createPacilianFallbackResponse(UUID pacilianId) {
        recordFallbackMetrics();
        PacilianPublicDto fallback = createDefaultPacilian(pacilianId);
        cachePacilianData(pacilianId, fallback, FALLBACK_CACHE_TTL);
        return CompletableFuture.completedFuture(fallback);
    }

    private CompletableFuture<CaregiverPublicDto> handleCaregiverServiceError(UUID caregiverId, Exception e) {
        recordErrorMetrics();
        log.error("Failed to fetch caregiver data for ID: {}: {}", caregiverId, e.getMessage());
        return createCaregiverFallbackResponse(caregiverId);
    }

    private CompletableFuture<PacilianPublicDto> handlePacilianServiceError(UUID pacilianId, Exception e) {
        recordErrorMetrics();
        log.error("Failed to fetch pacilian data for ID: {}: {}", pacilianId, e.getMessage());
        return createPacilianFallbackResponse(pacilianId);
    }

    private CaregiverPublicDto handleCaregiverFetchError(UUID caregiverId, Exception e) {
        log.error("Error in sync caregiver fetch for ID: {}: {}", caregiverId, e.getMessage());
        recordErrorMetrics();
        recordFallbackMetrics();
        return createDefaultCaregiver(caregiverId);
    }

    private PacilianPublicDto handlePacilianFetchError(UUID pacilianId, Exception e) {
        log.error("Error in sync pacilian fetch for ID: {}: {}", pacilianId, e.getMessage());
        recordErrorMetrics();
        recordFallbackMetrics();
        return createDefaultPacilian(pacilianId);
    }

    private void recordErrorMetrics() {
        userDataFetchErrorCounter.increment();
    }

    private void recordFallbackMetrics() {
        userDataFallbackCounter.increment();
    }

    private HttpEntity<?> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        return new HttpEntity<>(headers);
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

    private void cleanupExpiredCache() {
        cleanupExpiredCaregiverCache();
        cleanupExpiredPacilianCache();
    }

    private void cleanupExpiredCaregiverCache() {
        caregiverCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void cleanupExpiredPacilianCache() {
        pacilianCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public void clearCache() {
        caregiverCache.clear();
        pacilianCache.clear();
        log.info("User data cache cleared");
    }
    
    public void clearExpiredCache() {
        cleanupExpiredCache();
        log.info("Expired cache entries cleaned up");
    }

    private static class CacheEntry<T> {
        private final T value;
        private final LocalDateTime timestamp;
        private final Duration ttl;
        
        public CacheEntry(T value, LocalDateTime timestamp) {
            this(value, timestamp, CACHE_TTL);
        }
        
        public CacheEntry(T value, LocalDateTime timestamp, Duration ttl) {
            this.value = value;
            this.timestamp = timestamp;
            this.ttl = ttl;
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(timestamp.plus(ttl));
        }
    }
}