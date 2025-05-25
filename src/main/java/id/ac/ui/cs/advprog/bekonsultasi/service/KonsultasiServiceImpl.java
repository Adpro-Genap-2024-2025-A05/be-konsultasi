package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.function.Supplier;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;

@Service
@RequiredArgsConstructor
@Slf4j
public class KonsultasiServiceImpl implements KonsultasiService {
    private final KonsultasiRepository konsultasiRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final UserDataService userDataService;

    private final Counter konsultasiCreatedCounter;
    private final Counter konsultasiConfirmedCounter;
    private final Counter konsultasiCancelledCounter;
    private final Counter konsultasiCompletedCounter;
    private final Counter konsultasiRescheduledCounter;
    private final Counter konsultasiUpdateRequestCounter;
    private final Counter konsultasiRescheduleAcceptedCounter;
    private final Counter konsultasiRescheduleRejectedCounter;
    private final Counter konsultasiErrorCounter;
    private final Counter konsultasiScheduleConflictCounter;
    private final Counter konsultasiStateTransitionErrorCounter;

    private static final List<String> COMPLETED_STATUSES = List.of("CANCELLED", "DONE");
    private static final String REQUESTED_STATUS = "REQUESTED";
    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final String DONE_STATUS = "DONE";
    private static final String RESCHEDULED_STATUS = "RESCHEDULED";
    private static final String CAREGIVER_ROLE = "CAREGIVER";
    private static final String PACILIAN_ROLE = "PACILIAN";
    private static final int CONSULTATION_DURATION_HOURS = 1;

    @Override
    @Transactional
    public KonsultasiResponseDto createKonsultasi(CreateKonsultasiDto dto, UUID pacilianId) {
        log.info("Creating konsultasi for pacilian: {}, schedule: {}", pacilianId, dto.getScheduleId());
        
        return executeWithErrorHandling(() -> {
            Schedule schedule = findScheduleById(dto.getScheduleId());
            LocalDateTime scheduleDateTime = dto.getScheduleDateTime();

            validateScheduleAvailability(dto.getScheduleId(), scheduleDateTime);
            validateNoTimeConflictForUser(pacilianId, scheduleDateTime, null);

            Konsultasi konsultasi = buildNewKonsultasi(dto, pacilianId, schedule, scheduleDateTime);
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            
            konsultasiCreatedCounter.increment();
            log.info("Successfully created konsultasi: {}", savedKonsultasi.getId());
            return convertToResponseDto(savedKonsultasi);
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto confirmKonsultasi(UUID konsultasiId, UUID caregiverId) {
        log.info("Confirming konsultasi: {} by caregiver: {}", konsultasiId, caregiverId);
        
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, caregiverId, CAREGIVER_ROLE);
            validateNotRescheduled(konsultasi);

            return executeStateTransition(konsultasi, () -> {
                konsultasi.confirm();
                konsultasiConfirmedCounter.increment();
                log.info("Successfully confirmed konsultasi: {}", konsultasiId);
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto cancelKonsultasi(UUID konsultasiId, UUID userId, String role) {
        log.info("Cancelling konsultasi: {} by user: {} ({})", konsultasiId, userId, role);
        
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, userId, role);
            validateStatusEquals(konsultasi, REQUESTED_STATUS, 
                "Consultation can only be cancelled when in REQUESTED state");

            return executeStateTransition(konsultasi, () -> {
                konsultasi.cancel();
                konsultasiCancelledCounter.increment();
                log.info("Successfully cancelled konsultasi: {}", konsultasiId);
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto completeKonsultasi(UUID konsultasiId, UUID caregiverId) {
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, caregiverId, CAREGIVER_ROLE);

            return executeStateTransition(konsultasi, () -> {
                konsultasi.complete();
                konsultasiCompletedCounter.increment();
                log.info("Successfully completed konsultasi: {}", konsultasiId);
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto updateKonsultasiRequest(UUID konsultasiId, UpdateKonsultasiRequestDto dto, UUID pacilianId) {
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, pacilianId, PACILIAN_ROLE);
            validateStatusEquals(konsultasi, REQUESTED_STATUS, 
                "Consultation request can only be updated when in REQUESTED state");

            UUID targetScheduleId = determineTargetScheduleId(konsultasi, dto);
            validateScheduleAvailability(targetScheduleId, dto.getNewScheduleDateTime());
            validateNoTimeConflictForUser(pacilianId, dto.getNewScheduleDateTime(), konsultasiId);

            updateKonsultasiFields(konsultasi, dto, targetScheduleId);
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            konsultasiUpdateRequestCounter.increment();
            log.info("Successfully updated konsultasi request: {}", konsultasiId);
            return convertToResponseDto(savedKonsultasi);
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rescheduleKonsultasi(UUID konsultasiId, RescheduleKonsultasiDto dto, UUID caregiverId) {
        log.info("Rescheduling konsultasi: {} to {}", konsultasiId, dto.getNewScheduleDateTime());
        
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, caregiverId, CAREGIVER_ROLE);
            validateStatusEquals(konsultasi, CONFIRMED_STATUS, 
                "Consultation can only be rescheduled when in CONFIRMED state");

            UUID targetScheduleId = determineTargetScheduleIdForReschedule(konsultasi, dto);
            validateScheduleAvailability(targetScheduleId, dto.getNewScheduleDateTime());

            LocalDateTime currentDateTime = konsultasi.getScheduleDateTime();

            return executeStateTransition(konsultasi, () -> {
                updateKonsultasiForReschedule(konsultasi, dto, targetScheduleId, currentDateTime);
                konsultasiRescheduledCounter.increment();
                log.info("Successfully rescheduled konsultasi: {}", konsultasiId);
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto acceptReschedule(UUID konsultasiId, UUID pacilianId) {
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, pacilianId, PACILIAN_ROLE);
            validateStatusEquals(konsultasi, RESCHEDULED_STATUS, 
                "Only rescheduled consultations can be accepted");

            return executeStateTransition(konsultasi, () -> {
                konsultasi.confirm();
                konsultasiRescheduleAcceptedCounter.increment();
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rejectReschedule(UUID konsultasiId, UUID caregiverId) {
        return executeWithErrorHandling(() -> {
            Konsultasi konsultasi = findKonsultasiById(konsultasiId);
            validateUserRoleAndOwnership(konsultasi, caregiverId, PACILIAN_ROLE);
            validateStatusEquals(konsultasi, RESCHEDULED_STATUS, 
                "Only rescheduled consultations can be rejected");

            return executeStateTransition(konsultasi, () -> {
                RescheduledState rescheduledState = (RescheduledState) konsultasi.getState();
                rescheduledState.reject(konsultasi);
                konsultasiRescheduleRejectedCounter.increment();
                return konsultasiRepository.save(konsultasi);
            });
        });
    }

    @Override
    public KonsultasiResponseDto getKonsultasiById(UUID konsultasiId, UUID userId, String role) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, userId, role);
        return convertToResponseDtoByRole(konsultasi, role);
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByPacilianId(UUID pacilianId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByPacilianId(pacilianId);
        return konsultasiList.stream()
                .map(k -> convertToResponseDtoByRole(k, PACILIAN_ROLE))
                .toList();
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByCaregiverId(UUID caregiverId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByCaregiverId(caregiverId);
        return konsultasiList.stream()
                .map(k -> convertToResponseDtoByRole(k, CAREGIVER_ROLE))
                .toList();
    }

    @Override
    public List<KonsultasiResponseDto> getRequestedKonsultasiByCaregiverId(UUID caregiverId) {
        return convertToDtoList(konsultasiRepository.findByStatusAndCaregiverId(REQUESTED_STATUS, caregiverId));
    }


    private KonsultasiResponseDto executeWithErrorHandling(Supplier<KonsultasiResponseDto> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            log.error("Konsultasi operation failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private KonsultasiResponseDto executeStateTransition(Konsultasi konsultasi, Supplier<Konsultasi> operation) {
        initializeState(konsultasi);
        try {
            Konsultasi savedKonsultasi = operation.get();
            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            konsultasiStateTransitionErrorCounter.increment();
            throw new ScheduleException(e.getMessage());
        }
    }

    private void validateScheduleAvailability(UUID scheduleId, LocalDateTime dateTime) {
        if (!scheduleService.isScheduleAvailableForDateTime(scheduleId, dateTime)) {
            konsultasiScheduleConflictCounter.increment();
            throw new ScheduleException("Schedule is not available at the requested date and time");
        }
    }

    private void validateNoTimeConflictForUser(UUID pacilianId, LocalDateTime scheduleDateTime, UUID excludeKonsultasiId) {
        List<Konsultasi> activeKonsultations = konsultasiRepository.findByPacilianIdAndStatusNotIn(
                pacilianId, COMPLETED_STATUSES);

        for (Konsultasi existingKonsultasi : activeKonsultations) {
            if (excludeKonsultasiId != null && existingKonsultasi.getId().equals(excludeKonsultasiId)) {
                continue;
            }

            if (hasTimeConflict(existingKonsultasi.getScheduleDateTime(), scheduleDateTime)) {
                konsultasiScheduleConflictCounter.increment();
                throw new ScheduleException("You already have another consultation scheduled at this time");
            }
        }
    }

    private boolean hasTimeConflict(LocalDateTime existingDateTime, LocalDateTime newDateTime) {
        LocalDateTime existingStart = existingDateTime;
        LocalDateTime existingEnd = existingStart.plusHours(CONSULTATION_DURATION_HOURS);
        LocalDateTime newEnd = newDateTime.plusHours(CONSULTATION_DURATION_HOURS);

        return (newDateTime.isBefore(existingEnd) || newDateTime.isEqual(existingEnd)) &&
               (newEnd.isAfter(existingStart) || newEnd.isEqual(existingStart));
    }

    private void validateNotRescheduled(Konsultasi konsultasi) {
        if (RESCHEDULED_STATUS.equals(konsultasi.getStatus())) {
            throw new ScheduleException(
                "Rescheduled consultations must be accepted or rejected through the appropriate endpoints");
        }
    }

    private void validateStatusEquals(Konsultasi konsultasi, String expectedStatus, String errorMessage) {
        if (!expectedStatus.equals(konsultasi.getStatus())) {
            throw new ScheduleException(errorMessage);
        }
    }

    private UUID determineTargetScheduleId(Konsultasi konsultasi, UpdateKonsultasiRequestDto dto) {
        if (dto.getNewScheduleId() != null) {
            Schedule newSchedule = findScheduleById(dto.getNewScheduleId());
            validateSameCaregiver(newSchedule, konsultasi, "Cannot change to a different caregiver's schedule");
            return dto.getNewScheduleId();
        }
        return konsultasi.getScheduleId();
    }

    private UUID determineTargetScheduleIdForReschedule(Konsultasi konsultasi, RescheduleKonsultasiDto dto) {
        if (dto.getNewScheduleId() != null) {
            Schedule newSchedule = findScheduleById(dto.getNewScheduleId());
            validateSameCaregiver(newSchedule, konsultasi, "Cannot reschedule to a different caregiver's schedule");
            return dto.getNewScheduleId();
        }
        return konsultasi.getScheduleId();
    }

    private void validateSameCaregiver(Schedule newSchedule, Konsultasi konsultasi, String errorMessage) {
        if (!newSchedule.getCaregiverId().equals(konsultasi.getCaregiverId())) {
            throw new ScheduleException(errorMessage);
        }
    }

    private void updateKonsultasiFields(Konsultasi konsultasi, UpdateKonsultasiRequestDto dto, UUID targetScheduleId) {
        if (dto.getNewScheduleId() != null) {
            konsultasi.setScheduleId(targetScheduleId);
        }
        konsultasi.setScheduleDateTime(dto.getNewScheduleDateTime());
        if (dto.getNotes() != null) {
            konsultasi.setNotes(dto.getNotes());
        }
    }

    private void updateKonsultasiForReschedule(Konsultasi konsultasi, RescheduleKonsultasiDto dto, 
                                             UUID targetScheduleId, LocalDateTime currentDateTime) {
        if (dto.getNewScheduleId() != null) {
            konsultasi.setScheduleId(targetScheduleId);
        }
        konsultasi.setOriginalScheduleDateTime(currentDateTime);
        konsultasi.reschedule(dto.getNewScheduleDateTime());
        if (dto.getNotes() != null) {
            konsultasi.setNotes(dto.getNotes());
        }
    }

    private Schedule findScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException("Schedule not found"));
    }

    private Konsultasi findKonsultasiById(UUID konsultasiId) {
        return konsultasiRepository.findById(konsultasiId)
                .orElseThrow(() -> new ScheduleException("Consultation not found"));
    }

    private void validateUserRoleAndOwnership(Konsultasi konsultasi, UUID userId, String role) {
        validateRole(role);
        
        if (CAREGIVER_ROLE.equals(role)) {
            validateCaregiverOwnership(konsultasi, userId);
        } else if (PACILIAN_ROLE.equals(role)) {
            validatePacilianOwnership(konsultasi, userId);
        }
    }

    private void validateRole(String role) {
        if (!CAREGIVER_ROLE.equals(role) && !PACILIAN_ROLE.equals(role)) {
            throw new ScheduleException("Invalid role: " + role);
        }
    }

    private void validateCaregiverOwnership(Konsultasi konsultasi, UUID caregiverId) {
        if (!konsultasi.getCaregiverId().equals(caregiverId)) {
            throw new ScheduleException("You are not the caregiver for this consultation");
        }
    }

    private void validatePacilianOwnership(Konsultasi konsultasi, UUID pacilianId) {
        if (!konsultasi.getPacilianId().equals(pacilianId)) {
            throw new ScheduleException("You are not the patient for this consultation");
        }
    }

    private void initializeState(Konsultasi konsultasi) {
        switch (konsultasi.getStatus()) {
            case REQUESTED_STATUS -> konsultasi.setState(new RequestedState());
            case CONFIRMED_STATUS -> konsultasi.setState(new ConfirmedState());
            case CANCELLED_STATUS -> konsultasi.setState(new CancelledState());
            case DONE_STATUS -> konsultasi.setState(new DoneState());
            case RESCHEDULED_STATUS -> konsultasi.setState(new RescheduledState());
            default -> throw new IllegalStateException("Unknown consultation status: " + konsultasi.getStatus());
        }
    }

    private Konsultasi buildNewKonsultasi(CreateKonsultasiDto dto, UUID pacilianId,
                                        Schedule schedule, LocalDateTime scheduleDateTime) {
        Konsultasi konsultasi = Konsultasi.builder()
                .scheduleId(dto.getScheduleId())
                .caregiverId(schedule.getCaregiverId())
                .pacilianId(pacilianId)
                .scheduleDateTime(scheduleDateTime)
                .notes(dto.getNotes())
                .status(REQUESTED_STATUS)
                .build();

        konsultasi.setState(new RequestedState());
        return konsultasi;
    }

    private List<KonsultasiResponseDto> convertToDtoList(List<Konsultasi> konsultasiList) {
        return konsultasiList.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    private KonsultasiResponseDto convertToResponseDto(Konsultasi konsultasi) {
        return KonsultasiResponseDto.builder()
                .id(konsultasi.getId())
                .scheduleId(konsultasi.getScheduleId())
                .caregiverId(konsultasi.getCaregiverId())
                .pacilianId(konsultasi.getPacilianId())
                .scheduleDateTime(konsultasi.getScheduleDateTime())
                .notes(konsultasi.getNotes())
                .status(konsultasi.getStatus())
                .lastUpdated(konsultasi.getLastUpdated())
                .build();
    }

    private KonsultasiResponseDto convertToResponseDtoByRole(Konsultasi konsultasi, String role) {
        try {
            if (CAREGIVER_ROLE.equalsIgnoreCase(role)) {
                return buildResponseDtoForCaregiver(konsultasi);
            } else {
                return buildResponseDtoForPacilian(konsultasi);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return buildBasicResponseDto(konsultasi);
        }
    }

    private KonsultasiResponseDto buildResponseDtoForCaregiver(Konsultasi konsultasi) 
            throws InterruptedException, ExecutionException {
        CompletableFuture<PacilianPublicDto> pacilianFuture = userDataService
                .getPacilianByIdAsync(konsultasi.getPacilianId());
        
        PacilianPublicDto pacilianData = null;
        if (pacilianFuture != null) {
            pacilianData = pacilianFuture.get();
        }
        
        return KonsultasiResponseDto.builder()
                .id(konsultasi.getId())
                .scheduleId(konsultasi.getScheduleId())
                .caregiverId(konsultasi.getCaregiverId())
                .pacilianId(konsultasi.getPacilianId())
                .scheduleDateTime(konsultasi.getScheduleDateTime())
                .notes(konsultasi.getNotes())
                .status(konsultasi.getStatus())
                .lastUpdated(konsultasi.getLastUpdated())
                .caregiverData(null)
                .pacilianData(pacilianData)
                .build();
    }

    private KonsultasiResponseDto buildResponseDtoForPacilian(Konsultasi konsultasi) 
            throws InterruptedException, ExecutionException {
        CompletableFuture<CaregiverPublicDto> caregiverFuture = userDataService
                .getCaregiverByIdAsync(konsultasi.getCaregiverId());
        
        CaregiverPublicDto caregiverData = null;
        if (caregiverFuture != null) {
            caregiverData = caregiverFuture.get();
        }
        
        return KonsultasiResponseDto.builder()
                .id(konsultasi.getId())
                .scheduleId(konsultasi.getScheduleId())
                .caregiverId(konsultasi.getCaregiverId())
                .pacilianId(konsultasi.getPacilianId())
                .scheduleDateTime(konsultasi.getScheduleDateTime())
                .notes(konsultasi.getNotes())
                .status(konsultasi.getStatus())
                .lastUpdated(konsultasi.getLastUpdated())
                .caregiverData(caregiverData)
                .pacilianData(null)
                .build();
    }

    private KonsultasiResponseDto buildBasicResponseDto(Konsultasi konsultasi) {
        return KonsultasiResponseDto.builder()
                .id(konsultasi.getId())
                .scheduleId(konsultasi.getScheduleId())
                .caregiverId(konsultasi.getCaregiverId())
                .pacilianId(konsultasi.getPacilianId())
                .scheduleDateTime(konsultasi.getScheduleDateTime())
                .notes(konsultasi.getNotes())
                .status(konsultasi.getStatus())
                .lastUpdated(konsultasi.getLastUpdated())
                .caregiverData(null)
                .pacilianData(null)
                .build();
    }
}