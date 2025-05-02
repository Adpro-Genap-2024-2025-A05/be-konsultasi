package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class KonsultasiServiceImplTest {

    @Mock
    private KonsultasiRepository konsultasiRepository;
    
    @Mock
    private KonsultasiHistoryRepository historyRepository;
    
    @Mock
    private ScheduleRepository scheduleRepository;
    
    private KonsultasiService konsultasiService;
    
    private UUID konsultasiId;
    private UUID scheduleId;
    private UUID caregiverId;
    private UUID pacilianId;
    private LocalDateTime scheduleDateTime;
    private Schedule schedule;
    private Konsultasi konsultasi;
    private KonsultasiHistory konsultasiHistory;

    @BeforeEach
    void setUp() {
        openMocks(this);
        
        konsultasiService = new KonsultasiServiceImpl(
                konsultasiRepository,
                historyRepository,
                scheduleRepository
        );
        
        konsultasiId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        caregiverId = UUID.randomUUID();
        pacilianId = UUID.randomUUID();
        scheduleDateTime = LocalDateTime.now().plusDays(7);
        
        schedule = Schedule.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
        
        konsultasi = Konsultasi.builder()
                .id(konsultasiId)
                .scheduleId(scheduleId)
                .caregiverId(caregiverId)
                .pacilianId(pacilianId)
                .scheduleDateTime(scheduleDateTime)
                .notes("Test notes")
                .status("REQUESTED")
                .build();
        konsultasi.setState(new RequestedState());
        
        konsultasiHistory = KonsultasiHistory.builder()
                .id(UUID.randomUUID())
                .konsultasiId(konsultasiId)
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .timestamp(LocalDateTime.now())
                .modifiedBy(pacilianId)
                .notes("Consultation requested")
                .build();
    }

    @Test
    void testCreateKonsultasi() {
        CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Test consultation")
                .build();
        
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
            Konsultasi k = invocation.getArgument(0);
            k.setId(konsultasiId);
            return k;
        });
        
        KonsultasiResponseDto result = konsultasiService.createKonsultasi(dto, pacilianId);
        
        assertNotNull(result);
        assertEquals(konsultasiId, result.getId());
        assertEquals(scheduleId, result.getScheduleId());
        assertEquals(caregiverId, result.getCaregiverId());
        assertEquals(pacilianId, result.getPacilianId());
        assertEquals("REQUESTED", result.getStatus());
        
        verify(konsultasiRepository).save(any(Konsultasi.class));
        verify(historyRepository).save(any(KonsultasiHistory.class));
    }
    
    @Test
    void testGetKonsultasiHistory() {
        UUID historyId = UUID.randomUUID();
        
        KonsultasiHistory history = KonsultasiHistory.builder()
                .id(historyId) 
                .konsultasiId(konsultasiId)
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .timestamp(LocalDateTime.now())
                .modifiedBy(pacilianId)
                .notes("Consultation requested")
                .build();
        
        when(historyRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId))
                .thenReturn(Arrays.asList(history));
        
        List<KonsultasiHistoryDto> results = konsultasiService.getKonsultasiHistory(konsultasiId);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        KonsultasiHistoryDto result = results.get(0);
        assertEquals(historyId, result.getId()); 
        assertEquals("NONE", result.getPreviousStatus());
        assertEquals("REQUESTED", result.getNewStatus());
        assertEquals(pacilianId.toString(), result.getModifiedByUserType());
        assertEquals("Consultation requested", result.getNotes());
    }
}