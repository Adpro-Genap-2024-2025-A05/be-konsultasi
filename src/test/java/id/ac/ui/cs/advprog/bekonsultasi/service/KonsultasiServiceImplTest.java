package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KonsultasiServiceImplTest {

        @Mock
        private KonsultasiRepository konsultasiRepository;

        @Mock
        private KonsultasiHistoryRepository historyRepository;

        @Mock
        private ScheduleRepository scheduleRepository;

        @Mock
        private ScheduleService scheduleService;

        private KonsultasiServiceImpl konsultasiService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                konsultasiService = new KonsultasiServiceImpl(
                                konsultasiRepository,
                                historyRepository,
                                scheduleRepository,
                                scheduleService);
        }

        @Test
        void createKonsultasi_shouldCreateNewKonsultasi() {
                UUID scheduleId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();

                CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                                .scheduleId(scheduleId)
                                .notes("Test notes")
                                .build();

                Schedule schedule = Schedule.builder()
                                .id(scheduleId)
                                .caregiverId(caregiverId)
                                .day(DayOfWeek.MONDAY)
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status("AVAILABLE")
                                .build();

                Konsultasi savedKonsultasi = Konsultasi.builder()
                                .id(UUID.randomUUID())
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(savedKonsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(any(UUID.class), anyString());

                KonsultasiResponseDto result = konsultasiService.createKonsultasi(dto, pacilianId);

                assertNotNull(result);
                assertEquals(scheduleId, result.getScheduleId());
                assertEquals(caregiverId, result.getCaregiverId());
                assertEquals(pacilianId, result.getPacilianId());
                assertEquals("REQUESTED", result.getStatus());

                verify(scheduleRepository).findById(scheduleId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService).updateScheduleStatus(scheduleId, "REQUESTED");
        }

        @Test
        void createKonsultasi_shouldThrowExceptionIfScheduleNotFound() {
                UUID scheduleId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();

                CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                                .scheduleId(scheduleId)
                                .notes("Test notes")
                                .build();

                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.createKonsultasi(dto, pacilianId);
                });

                verify(scheduleRepository).findById(scheduleId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void createKonsultasi_shouldThrowExceptionIfScheduleNotAvailable() {
                UUID scheduleId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();

                CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                                .scheduleId(scheduleId)
                                .notes("Test notes")
                                .build();

                Schedule schedule = Schedule.builder()
                                .id(scheduleId)
                                .caregiverId(caregiverId)
                                .day(DayOfWeek.MONDAY)
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status("REQUESTED")
                                .build();

                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.createKonsultasi(dto, pacilianId);
                });

                verify(scheduleRepository).findById(scheduleId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void confirmKonsultasi_shouldConfirmKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                Konsultasi confirmedKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(confirmedKonsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(any(UUID.class), anyString());

                KonsultasiResponseDto result = konsultasiService.confirmKonsultasi(konsultasiId, caregiverId);

                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService).updateScheduleStatus(scheduleId, "APPROVED");
        }

        @Test
        void cancelKonsultasi_shouldCancelKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                Konsultasi cancelledKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CANCELLED")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(cancelledKonsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(any(UUID.class), anyString());

                KonsultasiResponseDto result = konsultasiService.cancelKonsultasi(konsultasiId, caregiverId,
                                "CAREGIVER");

                assertNotNull(result);
                assertEquals("CANCELLED", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");
        }

        @Test
        void completeKonsultasi_shouldCompleteKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED")
                                .build();

                Konsultasi completedKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("DONE")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(completedKonsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(any(UUID.class), anyString());

                KonsultasiResponseDto result = konsultasiService.completeKonsultasi(konsultasiId, caregiverId);

                assertNotNull(result);
                assertEquals("DONE", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");
        }

        @Test
        void confirmKonsultasi_shouldThrowExceptionIfInRescheduledState() {
        UUID konsultasiId = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();

        Konsultasi konsultasi = Konsultasi.builder()
                .id(konsultasiId)
                .scheduleId(scheduleId)
                .caregiverId(caregiverId)
                .pacilianId(UUID.randomUUID())
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes")
                .status("RESCHEDULED")  
                .build();

        when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

        assertThrows(ScheduleException.class, () -> {
                konsultasiService.confirmKonsultasi(konsultasiId, caregiverId);
        });

        verify(konsultasiRepository).findById(konsultasiId);
        verify(konsultasiRepository, never()).save(any(Konsultasi.class));
        verify(historyRepository, never()).save(any(KonsultasiHistory.class));
        verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void rescheduleKonsultasi_shouldRescheduleKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(10);

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                Konsultasi rescheduledKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(newDateTime)
                                .notes("Test notes")
                                .status("RESCHEDULED")
                                .build();

                RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                                .newScheduleDateTime(newDateTime)
                                .notes("Rescheduled due to conflict")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(rescheduledKonsultasi);

                KonsultasiResponseDto result = konsultasiService.rescheduleKonsultasi(konsultasiId, dto, caregiverId,
                                "CAREGIVER");

                assertNotNull(result);
                assertEquals(newDateTime, result.getScheduleDateTime());
                assertEquals("RESCHEDULED", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void rescheduleKonsultasi_shouldCreateRescheduledState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();
                LocalDateTime originalDateTime = LocalDateTime.now().plusDays(7);
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(10);

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                Konsultasi rescheduledKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(newDateTime)
                                .originalScheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("RESCHEDULED")
                                .build();

                RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                                .newScheduleDateTime(newDateTime)
                                .notes("Rescheduled due to conflict")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(rescheduledKonsultasi);

                KonsultasiResponseDto result = konsultasiService.rescheduleKonsultasi(konsultasiId, dto, caregiverId,
                                "CAREGIVER");

                assertNotNull(result);
                assertEquals(newDateTime, result.getScheduleDateTime());
                assertEquals("RESCHEDULED", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void rescheduleKonsultasi_shouldThrowExceptionIfNotInRequestedState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(10);

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED")  
                                .build();

                RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                                .newScheduleDateTime(newDateTime)
                                .notes("Rescheduled due to conflict")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.rescheduleKonsultasi(konsultasiId, dto, caregiverId, "CAREGIVER");
                });

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
        }

        @Test
        void acceptReschedule_shouldConfirmRescheduledKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();
                LocalDateTime originalDateTime = LocalDateTime.now().plusDays(5);
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(newDateTime)
                                .originalScheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("RESCHEDULED")
                                .build();

                Konsultasi confirmedKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(newDateTime)
                                .originalScheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("CONFIRMED")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(confirmedKonsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(any(UUID.class), anyString());

                KonsultasiResponseDto result = konsultasiService.acceptReschedule(konsultasiId, caregiverId);

                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus());
                assertEquals(newDateTime, result.getScheduleDateTime());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService).updateScheduleStatus(scheduleId, "APPROVED");
        }

        @Test
        void rejectReschedule_shouldRevertToRequestedState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();
                LocalDateTime originalDateTime = LocalDateTime.now().plusDays(5);
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(newDateTime)
                                .originalScheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("RESCHEDULED")
                                .build();

                Konsultasi rejectedKonsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(originalDateTime)
                                .originalScheduleDateTime(originalDateTime)
                                .notes("Test notes")
                                .status("REQUESTED")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(rejectedKonsultasi);

                KonsultasiResponseDto result = konsultasiService.rejectReschedule(konsultasiId, caregiverId);

                assertNotNull(result);
                assertEquals("REQUESTED", result.getStatus());
                assertEquals(originalDateTime, result.getScheduleDateTime());

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository).save(any(Konsultasi.class));
                verify(historyRepository).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void acceptReschedule_shouldThrowExceptionIfNotInRescheduledState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("REQUESTED") 
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.acceptReschedule(konsultasiId, caregiverId);
                });

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void rejectReschedule_shouldThrowExceptionIfNotInRescheduledState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED") 
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.rejectReschedule(konsultasiId, caregiverId);
                });

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void cancelKonsultasi_shouldThrowExceptionIfNotInRequestedState() {
                UUID konsultasiId = UUID.randomUUID();
                UUID caregiverId = UUID.randomUUID();
                UUID pacilianId = UUID.randomUUID();
                UUID scheduleId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(scheduleId)
                                .caregiverId(caregiverId)
                                .pacilianId(pacilianId)
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED")  
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () -> {
                        konsultasiService.cancelKonsultasi(konsultasiId, caregiverId, "CAREGIVER");
                });

                verify(konsultasiRepository).findById(konsultasiId);
                verify(konsultasiRepository, never()).save(any(Konsultasi.class));
                verify(historyRepository, never()).save(any(KonsultasiHistory.class));
                verify(scheduleService, never()).updateScheduleStatus(any(UUID.class), anyString());
        }

        @Test
        void getKonsultasiById_shouldReturnKonsultasi() {
                UUID konsultasiId = UUID.randomUUID();

                Konsultasi konsultasi = Konsultasi.builder()
                                .id(konsultasiId)
                                .scheduleId(UUID.randomUUID())
                                .caregiverId(UUID.randomUUID())
                                .pacilianId(UUID.randomUUID())
                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                .notes("Test notes")
                                .status("CONFIRMED")
                                .build();

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                KonsultasiResponseDto result = konsultasiService.getKonsultasiById(konsultasiId);

                assertNotNull(result);
                assertEquals(konsultasiId, result.getId());
                assertEquals("CONFIRMED", result.getStatus());

                verify(konsultasiRepository).findById(konsultasiId);
        }

        @Test
        void getKonsultasiByPacilianId_shouldReturnKonsultasiList() {
                UUID pacilianId = UUID.randomUUID();

                List<Konsultasi> konsultasiList = Arrays.asList(
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(UUID.randomUUID())
                                                .pacilianId(pacilianId)
                                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                                .notes("Test notes 1")
                                                .status("CONFIRMED")
                                                .build(),
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(UUID.randomUUID())
                                                .pacilianId(pacilianId)
                                                .scheduleDateTime(LocalDateTime.now().plusDays(14))
                                                .notes("Test notes 2")
                                                .status("REQUESTED")
                                                .build());

                when(konsultasiRepository.findByPacilianId(pacilianId)).thenReturn(konsultasiList);

                List<KonsultasiResponseDto> result = konsultasiService.getKonsultasiByPacilianId(pacilianId);

                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(pacilianId, result.get(0).getPacilianId());
                assertEquals(pacilianId, result.get(1).getPacilianId());

                verify(konsultasiRepository).findByPacilianId(pacilianId);
        }

        @Test
        void getKonsultasiByCaregiverId_shouldReturnKonsultasiList() {
                UUID caregiverId = UUID.randomUUID();

                List<Konsultasi> konsultasiList = Arrays.asList(
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(caregiverId)
                                                .pacilianId(UUID.randomUUID())
                                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                                .notes("Test notes 1")
                                                .status("CONFIRMED")
                                                .build(),
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(caregiverId)
                                                .pacilianId(UUID.randomUUID())
                                                .scheduleDateTime(LocalDateTime.now().plusDays(14))
                                                .notes("Test notes 2")
                                                .status("REQUESTED")
                                                .build());

                when(konsultasiRepository.findByCaregiverId(caregiverId)).thenReturn(konsultasiList);

                List<KonsultasiResponseDto> result = konsultasiService.getKonsultasiByCaregiverId(caregiverId);

                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(caregiverId, result.get(0).getCaregiverId());
                assertEquals(caregiverId, result.get(1).getCaregiverId());

                verify(konsultasiRepository).findByCaregiverId(caregiverId);
        }

        @Test
        void getRequestedKonsultasiByCaregiverId_shouldReturnRequestedKonsultasiList() {
                UUID caregiverId = UUID.randomUUID();

                List<Konsultasi> konsultasiList = Arrays.asList(
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(caregiverId)
                                                .pacilianId(UUID.randomUUID())
                                                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                                                .notes("Test notes 1")
                                                .status("REQUESTED")
                                                .build(),
                                Konsultasi.builder()
                                                .id(UUID.randomUUID())
                                                .scheduleId(UUID.randomUUID())
                                                .caregiverId(caregiverId)
                                                .pacilianId(UUID.randomUUID())
                                                .scheduleDateTime(LocalDateTime.now().plusDays(14))
                                                .notes("Test notes 2")
                                                .status("REQUESTED")
                                                .build());

                when(konsultasiRepository.findByStatusAndCaregiverId("REQUESTED", caregiverId))
                                .thenReturn(konsultasiList);

                List<KonsultasiResponseDto> result = konsultasiService.getRequestedKonsultasiByCaregiverId(caregiverId);

                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals("REQUESTED", result.get(0).getStatus());
                assertEquals("REQUESTED", result.get(1).getStatus());

                verify(konsultasiRepository).findByStatusAndCaregiverId("REQUESTED", caregiverId);
        }

        @Test
        void getKonsultasiHistory_shouldReturnHistoryList() {
                UUID konsultasiId = UUID.randomUUID();

                List<KonsultasiHistory> historyList = Arrays.asList(
                                KonsultasiHistory.builder()
                                                .id(UUID.randomUUID())
                                                .konsultasiId(konsultasiId)
                                                .previousStatus("NONE")
                                                .newStatus("REQUESTED")
                                                .timestamp(LocalDateTime.now().minusDays(2))
                                                .modifiedBy(UUID.randomUUID())
                                                .notes("Consultation requested")
                                                .build(),
                                KonsultasiHistory.builder()
                                                .id(UUID.randomUUID())
                                                .konsultasiId(konsultasiId)
                                                .previousStatus("REQUESTED")
                                                .newStatus("CONFIRMED")
                                                .timestamp(LocalDateTime.now().minusDays(1))
                                                .modifiedBy(UUID.randomUUID())
                                                .notes("Consultation confirmed by caregiver")
                                                .build());

                when(historyRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId)).thenReturn(historyList);

                List<KonsultasiHistoryDto> result = konsultasiService.getKonsultasiHistory(konsultasiId);

                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals("NONE", result.get(0).getPreviousStatus());
                assertEquals("REQUESTED", result.get(0).getNewStatus());
                assertEquals("REQUESTED", result.get(1).getPreviousStatus());
                assertEquals("CONFIRMED", result.get(1).getNewStatus());

                verify(historyRepository).findByKonsultasiIdOrderByTimestampDesc(konsultasiId);
        }
}