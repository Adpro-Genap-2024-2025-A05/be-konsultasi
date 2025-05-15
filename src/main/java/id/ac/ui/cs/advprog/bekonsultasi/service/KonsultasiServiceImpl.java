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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KonsultasiServiceImpl implements KonsultasiService {
    private final KonsultasiRepository konsultasiRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;

    @Override
    @Transactional
    public KonsultasiResponseDto createKonsultasi(CreateKonsultasiDto dto, UUID pacilianId) {
        Schedule schedule = findScheduleById(dto.getScheduleId());
        validateScheduleAvailability(schedule);

        LocalDateTime scheduleDateTime = calculateNextDateTimeForSchedule(schedule);

        List<String> completedStatuses = List.of("CANCELLED", "DONE");
        List<Konsultasi> activeKonsultations = konsultasiRepository.findByPacilianIdAndStatusNotIn(
                pacilianId, completedStatuses);

        for (Konsultasi existingKonsultasi : activeKonsultations) {
            LocalDateTime existingStart = existingKonsultasi.getScheduleDateTime();
            LocalDateTime existingEnd = existingStart.plusHours(1);

            LocalDateTime newEnd = scheduleDateTime.plusHours(1);

            if ((scheduleDateTime.isBefore(existingEnd) || scheduleDateTime.isEqual(existingEnd)) &&
                    (newEnd.isAfter(existingStart) || newEnd.isEqual(existingStart))) {
                throw new ScheduleException("You already have another consultation scheduled at this time");
            }
        }

        Konsultasi konsultasi = buildNewKonsultasi(dto, pacilianId, schedule, scheduleDateTime);

        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
        scheduleService.updateScheduleStatus(dto.getScheduleId(), "UNAVAILABLE");

        return convertToResponseDto(savedKonsultasi);
    }

    @Override
    @Transactional
    public KonsultasiResponseDto confirmKonsultasi(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

        if ("RESCHEDULED".equals(konsultasi.getStatus())) {
            throw new ScheduleException(
                    "Rescheduled consultations must be accepted or rejected through the appropriate endpoints");
        }

        initializeState(konsultasi);

        try {
            konsultasi.confirm();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto cancelKonsultasi(UUID konsultasiId, UUID userId, String role) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, userId, role);

        if (!"REQUESTED".equals(konsultasi.getStatus())) {
            throw new ScheduleException("Consultation can only be cancelled when in REQUESTED state");
        }

        initializeState(konsultasi);

        try {
            konsultasi.cancel();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            scheduleService.updateScheduleStatus(savedKonsultasi.getScheduleId(), "AVAILABLE");

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto completeKonsultasi(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

        initializeState(konsultasi);

        try {
            konsultasi.complete();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            scheduleService.updateScheduleStatus(savedKonsultasi.getScheduleId(), "AVAILABLE");

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rescheduleKonsultasi(UUID konsultasiId, RescheduleKonsultasiDto dto, UUID userId,
                                                      String role) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, userId, role);

        if (!"REQUESTED".equals(konsultasi.getStatus())) {
            throw new ScheduleException("Consultation can only be rescheduled when in REQUESTED state");
        }

        initializeState(konsultasi);

        try {
            konsultasi.reschedule(dto.getNewScheduleDateTime());

            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto acceptReschedule(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

        if (!"RESCHEDULED".equals(konsultasi.getStatus())) {
            throw new ScheduleException("Only rescheduled consultations can be accepted");
        }

        initializeState(konsultasi);

        try {
            konsultasi.confirm();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rejectReschedule(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findKonsultasiById(konsultasiId);
        validateUserRoleAndOwnership(konsultasi, caregiverId, "CAREGIVER");

        if (!"RESCHEDULED".equals(konsultasi.getStatus())) {
            throw new ScheduleException("Only rescheduled consultations can be rejected");
        }

        initializeState(konsultasi);

        try {
            RescheduledState rescheduledState = (RescheduledState) konsultasi.getState();
            rescheduledState.reject(konsultasi);

            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);

            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    public KonsultasiResponseDto getKonsultasiById(UUID konsultasiId) {
        return convertToResponseDto(findKonsultasiById(konsultasiId));
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByPacilianId(UUID pacilianId) {
        return convertToDtoList(konsultasiRepository.findByPacilianId(pacilianId));
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByCaregiverId(UUID caregiverId) {
        return convertToDtoList(konsultasiRepository.findByCaregiverId(caregiverId));
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

    private void validateScheduleAvailability(Schedule schedule) {
        if (!"AVAILABLE".equals(schedule.getStatus())) {
            throw new ScheduleException("Schedule is not available");
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

    private LocalDateTime calculateNextDateTimeForSchedule(Schedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek targetDay = schedule.getDay();
        LocalTime targetTime = schedule.getStartTime();

        LocalDateTime targetDateTime = now.with(TemporalAdjusters.nextOrSame(targetDay))
                .withHour(targetTime.getHour())
                .withMinute(targetTime.getMinute())
                .withSecond(0)
                .withNano(0);

        if (targetDateTime.isBefore(now) && targetDateTime.getDayOfWeek() == now.getDayOfWeek()) {
            targetDateTime = targetDateTime.plusWeeks(1);
        }

        return targetDateTime;
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
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}