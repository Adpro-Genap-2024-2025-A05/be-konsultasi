package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateKonsultasiDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.KonsultasiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.RescheduleKonsultasiDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.RequestedState;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import io.micrometer.core.instrument.Counter;

@ExtendWith(MockitoExtension.class)
class KonsultasiServiceImplTest {

        @Mock
        private KonsultasiRepository konsultasiRepository;

        @Mock
        private ScheduleRepository scheduleRepository;

        @Mock
        private ScheduleService scheduleService;

        @InjectMocks
        private KonsultasiServiceImpl konsultasiService;

        @Mock
        private Counter konsultasiCreatedCounter;
        @Mock
        private Counter konsultasiConfirmedCounter;
        @Mock
        private Counter konsultasiCancelledCounter;
        @Mock
        private Counter konsultasiCompletedCounter;
        @Mock
        private Counter konsultasiRescheduledCounter;
        @Mock
        private Counter konsultasiUpdateRequestCounter;
        @Mock
        private Counter konsultasiRescheduleAcceptedCounter;
        @Mock
        private Counter konsultasiRescheduleRejectedCounter;
        @Mock
        private Counter konsultasiErrorCounter;
        @Mock
        private Counter konsultasiScheduleConflictCounter;
        @Mock
        private Counter konsultasiStateTransitionErrorCounter;

        @Mock
        private UserDataService userDataService;

        private UUID pacilianId;
        private UUID caregiverId;
        private UUID scheduleId;
        private UUID newScheduleId;
        private UUID konsultasiId;
        private Schedule schedule;
        private Schedule newSchedule;
        private Konsultasi konsultasi;
        private CreateKonsultasiDto createDto;
        private RescheduleKonsultasiDto rescheduleDto;
        private LocalDateTime scheduleDateTime;

        @BeforeEach
        void setUp() {
                pacilianId = UUID.randomUUID();
                caregiverId = UUID.randomUUID();
                scheduleId = UUID.randomUUID();
                newScheduleId = UUID.randomUUID();
                konsultasiId = UUID.randomUUID();

                schedule = Schedule.builder()
                        .id(scheduleId)
                        .caregiverId(caregiverId)
                        .day(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 0))
                        .oneTime(false)
                        .build();

                newSchedule = Schedule.builder()
                        .id(newScheduleId)
                        .caregiverId(caregiverId)
                        .day(DayOfWeek.TUESDAY)
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(15, 0))
                        .oneTime(false)
                        .build();

                scheduleDateTime = LocalDateTime.now().plusDays(7);

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

                createDto = new CreateKonsultasiDto();
                createDto.setScheduleId(scheduleId);
                createDto.setScheduleDateTime(scheduleDateTime);
                createDto.setNotes("Test notes");

                rescheduleDto = new RescheduleKonsultasiDto();
                rescheduleDto.setNewScheduleDateTime(scheduleDateTime.plusDays(7));
                rescheduleDto.setNotes("Rescheduled");
        }

        @Test
        void testCreateKonsultasi() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(scheduleService.isScheduleAvailableForDateTime(eq(scheduleId), any(LocalDateTime.class))).thenReturn(true);
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(eq(pacilianId), any())).thenReturn(new ArrayList<>());
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(konsultasi);

                KonsultasiResponseDto response = konsultasiService.createKonsultasi(createDto, pacilianId);

                assertNotNull(response);
                assertEquals(konsultasiId, response.getId());
                assertEquals(scheduleId, response.getScheduleId());
                assertEquals(caregiverId, response.getCaregiverId());
                assertEquals(pacilianId, response.getPacilianId());
                assertEquals("REQUESTED", response.getStatus());

                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void testCreateKonsultasi_ScheduleNotAvailable() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(scheduleService.isScheduleAvailableForDateTime(eq(scheduleId), any(LocalDateTime.class))).thenReturn(false);

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.createKonsultasi(createDto, pacilianId));

                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
        }

        @Test
        void testRescheduleKonsultasi_DifferentCaregiverSchedule() {
                UUID differentCaregiverId = UUID.randomUUID();

                Schedule differentSchedule = Schedule.builder()
                        .id(newScheduleId)
                        .caregiverId(differentCaregiverId)
                        .day(DayOfWeek.TUESDAY)
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(15, 0))
                        .oneTime(false)
                        .build();

                rescheduleDto.setNewScheduleId(newScheduleId);

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(RuntimeException.class, () ->
                        konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId));

                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
        }

        @Test
        void testRescheduleKonsultasi_TimeConflict() {

                Konsultasi konflictingKonsultasi = Konsultasi.builder()
                        .id(UUID.randomUUID())
                        .scheduleId(scheduleId)
                        .caregiverId(caregiverId)
                        .pacilianId(UUID.randomUUID())
                        .scheduleDateTime(rescheduleDto.getNewScheduleDateTime())
                        .notes("Existing konsultasi")
                        .status("CONFIRMED")
                        .build();

                List<Konsultasi> existingKonsultasi = Arrays.asList(konflictingKonsultasi);

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(RuntimeException.class, () ->
                        konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId));

                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
        }

        @Test
        void testRescheduleKonsultasiNotInConfirmedState() {  
                Konsultasi requestedKonsultasi = Konsultasi.builder()
                        .id(konsultasiId)
                        .scheduleId(scheduleId)
                        .caregiverId(caregiverId)
                        .pacilianId(pacilianId)
                        .scheduleDateTime(LocalDateTime.now().plusDays(7))
                        .notes("Test notes")
                        .status("REQUESTED") 
                        .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(requestedKonsultasi));

                assertThrows(RuntimeException.class, () ->
                        konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId));

                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
        }
}