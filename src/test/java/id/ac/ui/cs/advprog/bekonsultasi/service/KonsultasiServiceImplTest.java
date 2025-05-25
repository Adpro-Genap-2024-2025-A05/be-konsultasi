package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Speciality;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.Counter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KonsultasiServiceImplTest {

        @Mock private KonsultasiRepository konsultasiRepository;
        @Mock private ScheduleRepository scheduleRepository;
        @Mock private ScheduleService scheduleService;
        @Mock private UserDataService userDataService;

        @Mock private Counter konsultasiCreatedCounter;
        @Mock private Counter konsultasiConfirmedCounter;
        @Mock private Counter konsultasiCancelledCounter;
        @Mock private Counter konsultasiCompletedCounter;
        @Mock private Counter konsultasiRescheduledCounter;
        @Mock private Counter konsultasiUpdateRequestCounter;
        @Mock private Counter konsultasiRescheduleAcceptedCounter;
        @Mock private Counter konsultasiRescheduleRejectedCounter;
        @Mock private Counter konsultasiErrorCounter;
        @Mock private Counter konsultasiScheduleConflictCounter;
        @Mock private Counter konsultasiStateTransitionErrorCounter;

        @InjectMocks
        private KonsultasiServiceImpl konsultasiService;

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
        private UpdateKonsultasiRequestDto updateDto;
        private LocalDateTime scheduleDateTime;
        private LocalDateTime futureDateTime;

        @BeforeEach
        void setUp() {
                pacilianId = UUID.randomUUID();
                caregiverId = UUID.randomUUID();
                scheduleId = UUID.randomUUID();
                newScheduleId = UUID.randomUUID();
                konsultasiId = UUID.randomUUID();

                scheduleDateTime = LocalDateTime.now().plusDays(7);
                futureDateTime = LocalDateTime.now().plusDays(14);

                schedule = createTestSchedule(scheduleId, caregiverId, DayOfWeek.MONDAY, 10, 11);
                newSchedule = createTestSchedule(newScheduleId, caregiverId, DayOfWeek.TUESDAY, 14, 15);

                konsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId, pacilianId, scheduleDateTime, "REQUESTED");

                createDto = createTestCreateDto();
                rescheduleDto = createTestRescheduleDto();
                updateDto = createTestUpdateDto();
        }

        private Schedule createTestSchedule(UUID id, UUID caregiverId, DayOfWeek day, int startHour, int endHour) {
                return Schedule.builder()
                        .id(id)
                        .caregiverId(caregiverId)
                        .day(day)
                        .startTime(LocalTime.of(startHour, 0))
                        .endTime(LocalTime.of(endHour, 0))
                        .oneTime(false)
                        .build();
        }

        private Konsultasi createTestKonsultasi(UUID id, UUID scheduleId, UUID caregiverId, UUID pacilianId,
                                                LocalDateTime dateTime, String status) {
                Konsultasi k = Konsultasi.builder()
                        .id(id)
                        .scheduleId(scheduleId)
                        .caregiverId(caregiverId)
                        .pacilianId(pacilianId)
                        .scheduleDateTime(dateTime)
                        .notes("Test notes")
                        .status(status)
                        .lastUpdated(LocalDateTime.now())
                        .build();

                switch (status) {
                        case "REQUESTED" -> k.setState(new RequestedState());
                        case "CONFIRMED" -> k.setState(new ConfirmedState());
                        case "CANCELLED" -> k.setState(new CancelledState());
                        case "DONE" -> k.setState(new DoneState());
                        case "RESCHEDULED" -> k.setState(new RescheduledState());
                }
                return k;
        }

        private CreateKonsultasiDto createTestCreateDto() {
                CreateKonsultasiDto dto = new CreateKonsultasiDto();
                dto.setScheduleId(scheduleId);
                dto.setScheduleDateTime(scheduleDateTime);
                dto.setNotes("Test notes");
                return dto;
        }

        private RescheduleKonsultasiDto createTestRescheduleDto() {
                RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto();
                dto.setNewScheduleDateTime(futureDateTime);
                dto.setNotes("Rescheduled");
                return dto;
        }

        private UpdateKonsultasiRequestDto createTestUpdateDto() {
                UpdateKonsultasiRequestDto dto = new UpdateKonsultasiRequestDto();
                dto.setNewScheduleDateTime(futureDateTime);
                dto.setNotes("Updated notes");
                return dto;
        }

        @Test
        void createKonsultasi_Success() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(scheduleService.isScheduleAvailableForDateTime(scheduleId, scheduleDateTime)).thenReturn(true);
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(pacilianId, Arrays.asList("CANCELLED", "DONE")))
                        .thenReturn(new ArrayList<>());
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(konsultasi);

                KonsultasiResponseDto response = konsultasiService.createKonsultasi(createDto, pacilianId);

                assertNotNull(response);
                assertEquals(konsultasiId, response.getId());
                assertEquals("REQUESTED", response.getStatus());
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void createKonsultasi_ScheduleNotFound() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.createKonsultasi(createDto, pacilianId));
        }

        @Test
        void createKonsultasi_ScheduleNotAvailable() {
                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(scheduleService.isScheduleAvailableForDateTime(scheduleId, scheduleDateTime)).thenReturn(false);

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.createKonsultasi(createDto, pacilianId));
        }

        @Test
        void createKonsultasi_TimeConflictWithExistingKonsultasi() {
                Konsultasi existingKonsultasi = createTestKonsultasi(UUID.randomUUID(), scheduleId,
                        caregiverId, pacilianId, scheduleDateTime, "CONFIRMED");

                when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
                when(scheduleService.isScheduleAvailableForDateTime(scheduleId, scheduleDateTime)).thenReturn(true);
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(pacilianId, Arrays.asList("CANCELLED", "DONE")))
                        .thenReturn(Arrays.asList(existingKonsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.createKonsultasi(createDto, pacilianId));
        }

        @Test
        void confirmKonsultasi_Success() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(confirmedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.confirmKonsultasi(konsultasiId, caregiverId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void confirmKonsultasi_KonsultasiNotFound() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.empty());

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.confirmKonsultasi(konsultasiId, caregiverId));
        }

        @Test
        void confirmKonsultasi_WrongCaregiver() {
                UUID wrongCaregiver = UUID.randomUUID();
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.confirmKonsultasi(konsultasiId, wrongCaregiver));
        }

        @Test
        void confirmKonsultasi_RescheduledStatus() {
                Konsultasi rescheduledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "RESCHEDULED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(rescheduledKonsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.confirmKonsultasi(konsultasiId, caregiverId));
        }

        @Test
        void cancelKonsultasi_SuccessAsPacilian() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                Konsultasi cancelledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CANCELLED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(cancelledKonsultasi);

                KonsultasiResponseDto response = konsultasiService.cancelKonsultasi(konsultasiId, pacilianId, "PACILIAN");

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void cancelKonsultasi_SuccessAsCaregiver() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                Konsultasi cancelledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CANCELLED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(cancelledKonsultasi);

                KonsultasiResponseDto response = konsultasiService.cancelKonsultasi(konsultasiId, caregiverId, "CAREGIVER");

                assertNotNull(response);
        }

        @Test
        void cancelKonsultasi_InvalidRole() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.cancelKonsultasi(konsultasiId, pacilianId, "INVALID_ROLE"));
        }

        @Test
        void cancelKonsultasi_WrongStatus() {
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.cancelKonsultasi(konsultasiId, pacilianId, "PACILIAN"));
        }

        @Test
        void completeKonsultasi_Success() {
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));

                Konsultasi completedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "DONE");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(completedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.completeKonsultasi(konsultasiId, caregiverId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void completeKonsultasi_WrongCaregiver() {
                UUID wrongCaregiver = UUID.randomUUID();
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.completeKonsultasi(konsultasiId, wrongCaregiver));
        }

        @Test
        void updateKonsultasiRequest_Success() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(scheduleService.isScheduleAvailableForDateTime(scheduleId, futureDateTime)).thenReturn(true);
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(pacilianId, Arrays.asList("CANCELLED", "DONE")))
                        .thenReturn(Arrays.asList(konsultasi));

                Konsultasi updatedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, futureDateTime, "REQUESTED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(updatedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.updateKonsultasiRequest(konsultasiId, updateDto, pacilianId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void updateKonsultasiRequest_WithNewSchedule() {
                updateDto.setNewScheduleId(newScheduleId);
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(newSchedule));
                when(scheduleService.isScheduleAvailableForDateTime(newScheduleId, futureDateTime)).thenReturn(true);
                when(konsultasiRepository.findByPacilianIdAndStatusNotIn(pacilianId, Arrays.asList("CANCELLED", "DONE")))
                        .thenReturn(Arrays.asList(konsultasi));

                Konsultasi updatedKonsultasi = createTestKonsultasi(konsultasiId, newScheduleId, caregiverId,
                        pacilianId, futureDateTime, "REQUESTED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(updatedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.updateKonsultasiRequest(konsultasiId, updateDto, pacilianId);

                assertNotNull(response);
        }

        @Test
        void updateKonsultasiRequest_WrongPacilian() {
                UUID wrongPacilian = UUID.randomUUID();
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.updateKonsultasiRequest(konsultasiId, updateDto, wrongPacilian));
        }

        @Test
        void updateKonsultasiRequest_WrongStatus() {
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.updateKonsultasiRequest(konsultasiId, updateDto, pacilianId));
        }

        @Test
        void updateKonsultasiRequest_DifferentCaregiverSchedule() {
                UUID differentCaregiverId = UUID.randomUUID();
                Schedule differentSchedule = createTestSchedule(newScheduleId, differentCaregiverId, DayOfWeek.TUESDAY, 14, 15);
                updateDto.setNewScheduleId(newScheduleId);

                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));
                when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(differentSchedule));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.updateKonsultasiRequest(konsultasiId, updateDto, pacilianId));
        }

        @Test
        void rescheduleKonsultasi_Success() {
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));
                when(scheduleService.isScheduleAvailableForDateTime(scheduleId, futureDateTime)).thenReturn(true);

                Konsultasi rescheduledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, futureDateTime, "RESCHEDULED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(rescheduledKonsultasi);

                KonsultasiResponseDto response = konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void rescheduleKonsultasi_WithNewSchedule() {
                rescheduleDto.setNewScheduleId(newScheduleId);
                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));
                when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(newSchedule));
                when(scheduleService.isScheduleAvailableForDateTime(newScheduleId, futureDateTime)).thenReturn(true);

                Konsultasi rescheduledKonsultasi = createTestKonsultasi(konsultasiId, newScheduleId, caregiverId,
                        pacilianId, futureDateTime, "RESCHEDULED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(rescheduledKonsultasi);

                KonsultasiResponseDto response = konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId);

                assertNotNull(response);
        }

        @Test
        void rescheduleKonsultasi_NotInConfirmedState() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId));
        }

        @Test
        void rescheduleKonsultasi_DifferentCaregiverSchedule() {
                UUID differentCaregiverId = UUID.randomUUID();
                Schedule differentSchedule = createTestSchedule(newScheduleId, differentCaregiverId, DayOfWeek.TUESDAY, 14, 15);
                rescheduleDto.setNewScheduleId(newScheduleId);

                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(confirmedKonsultasi));
                when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(differentSchedule));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.rescheduleKonsultasi(konsultasiId, rescheduleDto, caregiverId));
        }

        @Test
        void acceptReschedule_Success() {
                Konsultasi rescheduledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, futureDateTime, "RESCHEDULED");
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(rescheduledKonsultasi));

                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, futureDateTime, "CONFIRMED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(confirmedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.acceptReschedule(konsultasiId, pacilianId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void acceptReschedule_WrongStatus() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                assertThrows(ScheduleException.class, () ->
                        konsultasiService.acceptReschedule(konsultasiId, pacilianId));
        }

        @Test
        void rejectReschedule_Success() {
                Konsultasi rescheduledKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, futureDateTime, "RESCHEDULED");
                rescheduledKonsultasi.setOriginalScheduleDateTime(scheduleDateTime);
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(rescheduledKonsultasi));

                Konsultasi confirmedKonsultasi = createTestKonsultasi(konsultasiId, scheduleId, caregiverId,
                        pacilianId, scheduleDateTime, "CONFIRMED");
                when(konsultasiRepository.save(any(Konsultasi.class))).thenReturn(confirmedKonsultasi);

                KonsultasiResponseDto response = konsultasiService.rejectReschedule(konsultasiId, pacilianId);

                assertNotNull(response);
                verify(konsultasiRepository).save(any(Konsultasi.class));
        }

        @Test
        void getKonsultasiById_Success() {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                KonsultasiResponseDto response = konsultasiService.getKonsultasiById(konsultasiId, pacilianId, "PACILIAN");

                assertNotNull(response);
                assertEquals(konsultasiId, response.getId());
        }

        @Test
        void getKonsultasiById_WithCaregiverData() throws ExecutionException, InterruptedException {
                when(konsultasiRepository.findById(konsultasiId)).thenReturn(Optional.of(konsultasi));

                CaregiverPublicDto caregiverData = CaregiverPublicDto.builder()
                        .id(caregiverId.toString())
                        .name("Dr. Test")
                        .email("test@test.com")
                        .speciality(Speciality.DOKTER_UMUM)
                        .build();

                CompletableFuture<CaregiverPublicDto> future = CompletableFuture.completedFuture(caregiverData);
                when(userDataService.getCaregiverByIdAsync(caregiverId)).thenReturn(future);

                KonsultasiResponseDto response = konsultasiService.getKonsultasiById(konsultasiId, pacilianId, "PACILIAN");

                assertNotNull(response);
                assertNotNull(response.getCaregiverData());
                assertEquals("Dr. Test", response.getCaregiverData().getName());
        }
}