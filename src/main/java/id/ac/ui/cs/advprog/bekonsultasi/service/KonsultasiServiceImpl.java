package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiRepository;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    private final Timer konsultasiCreationTimer;
    private final Timer konsultasiProcessingTimer;

    @Override
    @Transactional
    public KonsultasiResponseDto createKonsultasi(CreateKonsultasiDto dto, UUID pacilianId) {
        try {
            return konsultasiCreationTimer.recordCallable(() -> {
                try {
                    Schedule schedule = findScheduleById(dto.getScheduleId());
                    LocalDateTime scheduleDateTime = dto.getScheduleDateTime();

                    if (!scheduleService.isScheduleAvailableForDateTime(dto.getScheduleId(), scheduleDateTime)) {
                        konsultasiScheduleConflictCounter.increment();
                        throw new ScheduleException("Schedule is not available at the requested date and time");
                    }

                    List<String> completedStatuses = List.of("CANCELLED", "DONE");
                    List<Konsultasi> activeKonsultations = konsultasiRepository.findByPacilianIdAndStatusNotIn(
                            pacilianId, completedStatuses);

                    for (Konsultasi existingKonsultasi : activeKonsultations) {
                        LocalDateTime existingStart = existingKonsultasi.getScheduleDateTime();
                        LocalDateTime existingEnd = existingStart.plusHours(1);
                        LocalDateTime newEnd = scheduleDateTime.plusHours(1);

                        if ((scheduleDateTime.isBefore(existingEnd) || scheduleDateTime.isEqual(existingEnd)) &&
                                (newEnd.isAfter(existingStart) || newEnd.isEqual(existingStart))) {
                            konsultasiScheduleConflictCounter.increment();
                            throw new ScheduleException("You already have another consultation scheduled at this time");
                        }
                    }

                    Konsultasi konsultasi = buildNewKonsultasi(dto, pacilianId, schedule, scheduleDateTime);
                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
                    
                    konsultasiCreatedCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (ScheduleException e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto confirmKonsultasi(UUID konsultasiId, UUID caregiverId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

                    if ("RESCHEDULED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Rescheduled consultations must be accepted or rejected through the appropriate endpoints");
                    }

                    initializeState(konsultasi);
                    konsultasi.confirm();
                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

                    konsultasiConfirmedCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto cancelKonsultasi(UUID konsultasiId, UUID userId, String role) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, userId, role);

                    if (!"REQUESTED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Consultation can only be cancelled when in REQUESTED state");
                    }

                    initializeState(konsultasi);
                    konsultasi.cancel();
                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

                    konsultasiCancelledCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto completeKonsultasi(UUID konsultasiId, UUID caregiverId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

                    initializeState(konsultasi);
                    konsultasi.complete();
                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

                    konsultasiCompletedCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto updateKonsultasiRequest(UUID konsultasiId, UpdateKonsultasiRequestDto dto, UUID pacilianId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, pacilianId, "PACILIAN");

                    if (!"REQUESTED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Consultation request can only be updated when in REQUESTED state");
                    }

                    UUID targetScheduleId = konsultasi.getScheduleId();

                    if (dto.getNewScheduleId() != null) {
                        Schedule newSchedule = findScheduleById(dto.getNewScheduleId());
                        if (!newSchedule.getCaregiverId().equals(konsultasi.getCaregiverId())) {
                            konsultasiErrorCounter.increment();
                            throw new ScheduleException("Cannot change to a different caregiver's schedule");
                        }
                        targetScheduleId = dto.getNewScheduleId();
                    }

                    if (!scheduleService.isScheduleAvailableForDateTime(targetScheduleId, dto.getNewScheduleDateTime())) {
                        konsultasiScheduleConflictCounter.increment();
                        throw new ScheduleException("The requested date and time are not available");
                    }

                    if (dto.getNewScheduleId() != null) {
                        konsultasi.setScheduleId(targetScheduleId);
                    }
                    konsultasi.setScheduleDateTime(dto.getNewScheduleDateTime());
                    if (dto.getNotes() != null) {
                        konsultasi.setNotes(dto.getNotes());
                    }

                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
                    konsultasiUpdateRequestCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rescheduleKonsultasi(UUID konsultasiId, RescheduleKonsultasiDto dto, UUID caregiverId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

                    if (!"CONFIRMED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Consultation can only be rescheduled when in CONFIRMED state");
                    }

                    LocalDateTime currentDateTime = konsultasi.getScheduleDateTime();
                    initializeState(konsultasi);

                    if (dto.getNewScheduleId() != null) {
                        konsultasi.setScheduleId(dto.getNewScheduleId());
                    }

                    konsultasi.setOriginalScheduleDateTime(currentDateTime);
                    konsultasi.reschedule(dto.getNewScheduleDateTime());

                    if (dto.getNotes() != null) {
                        konsultasi.setNotes(dto.getNotes());
                    }

                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
                    konsultasiRescheduledCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto acceptReschedule(UUID konsultasiId, UUID pacilianId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, pacilianId, "PACILIAN");

                    if (!"RESCHEDULED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Only rescheduled consultations can be accepted");
                    }

                    initializeState(konsultasi);
                    konsultasi.confirm();
                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

                    konsultasiRescheduleAcceptedCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rejectReschedule(UUID konsultasiId, UUID caregiverId) {
        try {
            return konsultasiProcessingTimer.recordCallable(() -> {
                try {
                    Konsultasi konsultasi = findKonsultasiById(konsultasiId);
                    validateUserRoleAndOwnership(konsultasi, caregiverId, "PACILIAN");

                    if (!"RESCHEDULED".equals(konsultasi.getStatus())) {
                        konsultasiStateTransitionErrorCounter.increment();
                        throw new ScheduleException("Only rescheduled consultations can be rejected");
                    }

                    initializeState(konsultasi);
                    RescheduledState rescheduledState = (RescheduledState) konsultasi.getState();
                    rescheduledState.reject(konsultasi);

                    Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
                    konsultasiRescheduleRejectedCounter.increment();
                    return convertToResponseDto(savedKonsultasi);
                } catch (IllegalStateException e) {
                    konsultasiStateTransitionErrorCounter.increment();
                    throw new ScheduleException(e.getMessage());
                } catch (Exception e) {
                    konsultasiErrorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            konsultasiErrorCounter.increment();
            throw new RuntimeException(e);
        }
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
                .map(k -> convertToResponseDtoByRole(k, "PACILIAN"))
                .collect(Collectors.toList());
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByCaregiverId(UUID caregiverId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByCaregiverId(caregiverId);
        return konsultasiList.stream()
                .map(k -> convertToResponseDtoByRole(k, "CAREGIVER"))
                .collect(Collectors.toList());
    }

    @Override
    public List<KonsultasiResponseDto> getRequestedKonsultasiByCaregiverId(UUID caregiverId) {
        return convertToDtoList(konsultasiRepository.findByStatusAndCaregiverId("REQUESTED", caregiverId));
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
        if (!"CAREGIVER".equals(role) && !"PACILIAN".equals(role)) {
            throw new ScheduleException("Invalid role: " + role);
        }

        if ("CAREGIVER".equals(role)) {
            if (!konsultasi.getCaregiverId().equals(userId)) {
                throw new ScheduleException("You are not the caregiver for this consultation");
            }
        } else if ("PACILIAN".equals(role)) {
            if (!konsultasi.getPacilianId().equals(userId)) {
                throw new ScheduleException("You are not the patient for this consultation");
            }
        }
    }

    private void initializeState(Konsultasi konsultasi) {
        switch (konsultasi.getStatus()) {
            case "REQUESTED" -> konsultasi.setState(new RequestedState());
            case "CONFIRMED" -> konsultasi.setState(new ConfirmedState());
            case "CANCELLED" -> konsultasi.setState(new CancelledState());
            case "DONE" -> konsultasi.setState(new DoneState());
            case "RESCHEDULED" -> konsultasi.setState(new RescheduledState());
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
                .status("REQUESTED")
                .build();

        konsultasi.setState(new RequestedState());
        return konsultasi;
    }

    private List<KonsultasiResponseDto> convertToDtoList(List<Konsultasi> konsultasiList) {
        return konsultasiList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
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
            if ("CAREGIVER".equalsIgnoreCase(role)) {
                CompletableFuture<PacilianPublicDto> pacilianFuture = userDataService
                        .getPacilianByIdAsync(konsultasi.getPacilianId());
                pacilianFuture.get();
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
                        .pacilianData(pacilianFuture.get())
                        .build();
            } else {
                CompletableFuture<CaregiverPublicDto> caregiverFuture = userDataService
                        .getCaregiverByIdAsync(konsultasi.getCaregiverId());
                caregiverFuture.get();
                return KonsultasiResponseDto.builder()
                        .id(konsultasi.getId())
                        .scheduleId(konsultasi.getScheduleId())
                        .caregiverId(konsultasi.getCaregiverId())
                        .pacilianId(konsultasi.getPacilianId())
                        .scheduleDateTime(konsultasi.getScheduleDateTime())
                        .notes(konsultasi.getNotes())
                        .status(konsultasi.getStatus())
                        .lastUpdated(konsultasi.getLastUpdated())
                        .caregiverData(caregiverFuture.get())
                        .pacilianData(null)
                        .build();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
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
}