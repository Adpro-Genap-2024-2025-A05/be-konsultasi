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
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        this.httpEntity = new HttpEntity<>(headers);
    }

    @Override
    @Async("userDataTaskExecutor")
    public CompletableFuture<CaregiverPublicDto> getCaregiverByIdAsync(UUID caregiverId) {
        log.info("Async fetching caregiver data for ID: {}", caregiverId);
        caregiverDataRequestCounter.increment();
        
        CacheEntry<CaregiverPublicDto> cached = caregiverCache.get(caregiverId);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.getValue());
        }
        
        try {
            String url = authServiceUrl + "/data/caregiver/" + caregiverId.toString();
            
            ResponseEntity<ApiResponseDto<CaregiverPublicDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ApiResponseDto<CaregiverPublicDto>>() {}
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                CaregiverPublicDto result = response.getBody().getData();
                caregiverCache.put(caregiverId, new CacheEntry<>(result, LocalDateTime.now()));
                log.info("Successfully fetched caregiver data for ID: {}", caregiverId);
                return CompletableFuture.completedFuture(result);
            }
            
            userDataFallbackCounter.increment();
            CaregiverPublicDto fallback = createDefaultCaregiver(caregiverId);
            caregiverCache.put(caregiverId, new CacheEntry<>(fallback, LocalDateTime.now(), Duration.ofMinutes(1)));
            return CompletableFuture.completedFuture(fallback);
            
        } catch (Exception e) {
            userDataFetchErrorCounter.increment();
            userDataFallbackCounter.increment();
            log.error("Failed to fetch caregiver data for ID: {}: {}", caregiverId, e.getMessage());
            
            CaregiverPublicDto fallback = createDefaultCaregiver(caregiverId);
            caregiverCache.put(caregiverId, new CacheEntry<>(fallback, LocalDateTime.now(), Duration.ofMinutes(1)));
            return CompletableFuture.completedFuture(fallback);
        }
    }

    @Override
    @Async("userDataTaskExecutor")
    public CompletableFuture<PacilianPublicDto> getPacilianByIdAsync(UUID pacilianId) {
        log.info("Async fetching pacilian data for ID: {}", pacilianId);
        pacilianDataRequestCounter.increment();
        
        CacheEntry<PacilianPublicDto> cached = pacilianCache.get(pacilianId);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.getValue());
        }
        
        try {
            String url = authServiceUrl + "/data/pacilian/" + pacilianId.toString();
            
            ResponseEntity<ApiResponseDto<PacilianPublicDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ApiResponseDto<PacilianPublicDto>>() {}
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                PacilianPublicDto result = response.getBody().getData();
                pacilianCache.put(pacilianId, new CacheEntry<>(result, LocalDateTime.now()));
                log.info("Successfully fetched pacilian data for ID: {}", pacilianId);
                return CompletableFuture.completedFuture(result);
            }
            
            userDataFallbackCounter.increment();
            PacilianPublicDto fallback = createDefaultPacilian(pacilianId);
            pacilianCache.put(pacilianId, new CacheEntry<>(fallback, LocalDateTime.now(), Duration.ofMinutes(1)));
            return CompletableFuture.completedFuture(fallback);
            
        } catch (Exception e) {
            userDataFetchErrorCounter.increment();
            userDataFallbackCounter.increment();
            log.error("Failed to fetch pacilian data for ID: {}: {}", pacilianId, e.getMessage());
            
            PacilianPublicDto fallback = createDefaultPacilian(pacilianId);
            pacilianCache.put(pacilianId, new CacheEntry<>(fallback, LocalDateTime.now(), Duration.ofMinutes(1)));
            return CompletableFuture.completedFuture(fallback);
        }
    }

    @Override
    public CaregiverPublicDto getCaregiverById(UUID caregiverId) {
        log.info("Sync fetching caregiver data for ID: {}", caregiverId);
        try {
            return getCaregiverByIdAsync(caregiverId).get();
        } catch (Exception e) {
            log.error("Error in sync caregiver fetch for ID: {}: {}", caregiverId, e.getMessage());
            userDataFetchErrorCounter.increment();
            userDataFallbackCounter.increment();
            return createDefaultCaregiver(caregiverId);
        }
    }

    @Override
    public PacilianPublicDto getPacilianById(UUID pacilianId) {
        log.info("Sync fetching pacilian data for ID: {}", pacilianId);
        try {
            return getPacilianByIdAsync(pacilianId).get();
        } catch (Exception e) {
            log.error("Error in sync pacilian fetch for ID: {}: {}", pacilianId, e.getMessage());
            userDataFetchErrorCounter.increment();
            userDataFallbackCounter.increment();
            return createDefaultPacilian(pacilianId);
        }
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
    
    private void cleanupExpiredCache() {
        caregiverCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        pacilianCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
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
    
    public void clearCache() {
        caregiverCache.clear();
        pacilianCache.clear();
        log.info("User data cache cleared");
    }
    
    public void clearExpiredCache() {
        cleanupExpiredCache();
        log.info("Expired cache entries cleaned up");
    }
}