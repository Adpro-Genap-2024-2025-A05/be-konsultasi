package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateKonsultasiDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.KonsultasiHistoryDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.KonsultasiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.RescheduleKonsultasiDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KonsultasiServiceImplTest {

        @Mock
        private KonsultasiRepository konsultasiRepository;

        @Mock
        private KonsultasiHistoryRepository historyRepository;

        @Mock
        private ScheduleRepository scheduleRepository;

        @Mock
        private ScheduleService scheduleService;

        @InjectMocks
        private KonsultasiServiceImpl konsultasiService;

        private UUID scheduleId;
        private UUID caregiverId;
        private UUID pacilianId;
        private Schedule schedule;
        private Konsultasi konsultasi;
        private CreateKonsultasiDto createDto;

        @BeforeEach
        void setUp() {
                scheduleId = UUID.randomUUID();
                caregiverId = UUID.randomUUID();
                pacilianId = UUID.randomUUID();

                schedule = Schedule.builder()
                        .id(scheduleId)
                        .caregiverId(caregiverId)
                        .day(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .status("AVAILABLE")
                        .build();

                konsultasi = Konsultasi.builder()
                        .id(UUID.randomUUID())
                        .scheduleId(scheduleId)
                        .caregiverId(caregiverId)
                        .pacilianId(pacilianId)
                        .scheduleDateTime(LocalDateTime.now().plusDays(7))
                        .notes("Test notes")
                        .status("REQUESTED")
                        .build();

                createDto = CreateKonsultasiDto.builder()
                        .scheduleId(scheduleId)
                        .notes("Test notes")
                        .build();
        }

        @Test
        void testCreateKonsultasi() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(eq(pacilianId), any())).thenReturn(new ArrayList<>());
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(konsultasi);
                doNothing().when(scheduleService).updateScheduleStatus(scheduleId, "UNAVAILABLE");

                KonsultasiResponseDto result = konsultasiService.createKonsultasi(createDto, pacilianId);

                assertNotNull(result);
                assertEquals(konsultasi.getId(), result.getId());
                assertEquals("REQUESTED", result.getStatus());
                verify(scheduleService).updateScheduleStatus(scheduleId, "UNAVAILABLE");
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testCreateKonsultasiScheduleNotAvailable() {
                schedule.setStatus("UNAVAILABLE");

                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.createKonsultasi(createDto, pacilianId)
                );
        }

        @Test
        void testConfirmKonsultasi() {
                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("CONFIRMED");
                        return k;
                });

                KonsultasiResponseDto result = konsultasiService.confirmKonsultasi(konsultasi.getId(), caregiverId);

                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus());
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testCancelKonsultasi() {
                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("CANCELLED");
                        return k;
                });
                doNothing().when(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");

                KonsultasiResponseDto result = konsultasiService.cancelKonsultasi(konsultasi.getId(), pacilianId, "PACILIAN");

                assertNotNull(result);
                assertEquals("CANCELLED", result.getStatus());
                verify(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testCompleteKonsultasi() {
                konsultasi.setStatus("CONFIRMED");

                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("DONE");
                        return k;
                });
                doNothing().when(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");

                KonsultasiResponseDto result = konsultasiService.completeKonsultasi(konsultasi.getId(), caregiverId);

                assertNotNull(result);
                assertEquals("DONE", result.getStatus());
                verify(scheduleService).updateScheduleStatus(scheduleId, "AVAILABLE");
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testRescheduleKonsultasi() {
                LocalDateTime newDateTime = LocalDateTime.now().plusDays(14);
                RescheduleKonsultasiDto rescheduleDto = RescheduleKonsultasiDto.builder()
                        .newScheduleDateTime(newDateTime)
                        .notes("Reschedule notes")
                        .build();

                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("RESCHEDULED");
                        k.setScheduleDateTime(newDateTime);
                        return k;
                });

                KonsultasiResponseDto result = konsultasiService.rescheduleKonsultasi(
                        konsultasi.getId(), rescheduleDto, pacilianId, "PACILIAN");

                assertNotNull(result);
                assertEquals("RESCHEDULED", result.getStatus());
                assertEquals(newDateTime, result.getScheduleDateTime());
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testAcceptReschedule() {
                konsultasi.setStatus("RESCHEDULED");

                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("CONFIRMED");
                        return k;
                });

                KonsultasiResponseDto result = konsultasiService.acceptReschedule(konsultasi.getId(), caregiverId);

                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus());
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testRejectReschedule() {
                konsultasi.setStatus("RESCHEDULED");
                LocalDateTime originalDateTime = LocalDateTime.now().plusDays(5);
                konsultasi.setOriginalScheduleDateTime(originalDateTime);

                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));
                when(konsultasiRepository.save(any(Konsultasi.class))).thenAnswer(invocation -> {
                        Konsultasi k = invocation.getArgument(0);
                        k.setStatus("REQUESTED");
                        k.setScheduleDateTime(originalDateTime);
                        return k;
                });

                KonsultasiResponseDto result = konsultasiService.rejectReschedule(konsultasi.getId(), caregiverId);

                assertNotNull(result);
                assertEquals("REQUESTED", result.getStatus());
                verify(historyRepository).save(any(KonsultasiHistory.class));
        }

        @Test
        void testGetKonsultasiById() {
                when(konsultasiRepository.findById(konsultasi.getId())).thenReturn(Optional.of(konsultasi));

                KonsultasiResponseDto result = konsultasiService.getKonsultasiById(konsultasi.getId());

                assertNotNull(result);
                assertEquals(konsultasi.getId(), result.getId());
        }

        @Test
        void testGetKonsultasiByPacilianId() {
                when(konsultasiRepository.findByPacilianId(pacilianId)).thenReturn(List.of(konsultasi));

                List<KonsultasiResponseDto> result = konsultasiService.getKonsultasiByPacilianId(pacilianId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(konsultasi.getId(), result.get(0).getId());
        }

        @Test
        void testGetKonsultasiByCaregiverId() {
                when(konsultasiRepository.findByCaregiverId(caregiverId)).thenReturn(List.of(konsultasi));

                List<KonsultasiResponseDto> result = konsultasiService.getKonsultasiByCaregiverId(caregiverId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(konsultasi.getId(), result.get(0).getId());
        }

        @Test
        void testGetRequestedKonsultasiByCaregiverId() {
                when(konsultasiRepository.findByStatusAndCaregiverId("REQUESTED", caregiverId)).thenReturn(List.of(konsultasi));

                List<KonsultasiResponseDto> result = konsultasiService.getRequestedKonsultasiByCaregiverId(caregiverId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(konsultasi.getId(), result.get(0).getId());
        }

        @Test
        void testGetKonsultasiHistory() {
                UUID konsultasiId = konsultasi.getId();
                KonsultasiHistory history = KonsultasiHistory.builder()
                        .id(UUID.randomUUID())
                        .konsultasiId(konsultasiId)
                        .previousStatus("NONE")
                        .newStatus("REQUESTED")
                        .timestamp(LocalDateTime.now())
                        .modifiedBy(pacilianId)
                        .notes("Test history")
                        .build();

                when(historyRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId)).thenReturn(List.of(history));

                List<KonsultasiHistoryDto> result = konsultasiService.getKonsultasiHistory(konsultasiId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(history.getId(), result.get(0).getId());
                assertEquals(history.getPreviousStatus(), result.get(0).getPreviousStatus());
                assertEquals(history.getNewStatus(), result.get(0).getNewStatus());
        }
}