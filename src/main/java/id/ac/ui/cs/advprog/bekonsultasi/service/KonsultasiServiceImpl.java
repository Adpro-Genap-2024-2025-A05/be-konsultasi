package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.model.Konsultasi;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiHistory;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.*;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.KonsultasiHistoryRepository;
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
    private final KonsultasiHistoryRepository historyRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public KonsultasiResponseDto createKonsultasi(CreateKonsultasiDto dto, UUID pacilianId) {
        Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new ScheduleException("Schedule not found"));
        
        if (!"AVAILABLE".equals(schedule.getStatus())) {
            throw new ScheduleException("Schedule is not available");
        }
        
        LocalDateTime scheduleDateTime = calculateNextDateTimeForSchedule(schedule);
        
        Konsultasi konsultasi = Konsultasi.builder()
                .scheduleId(dto.getScheduleId())
                .caregiverId(schedule.getCaregiverId())
                .pacilianId(pacilianId)
                .scheduleDateTime(scheduleDateTime)
                .notes(dto.getNotes())
                .status("REQUESTED")
                .build();
        
        konsultasi.setState(new RequestedState());
        Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
        
        KonsultasiHistory history = KonsultasiHistory.builder()
                .konsultasiId(savedKonsultasi.getId())
                .previousStatus("NONE")
                .newStatus("REQUESTED")
                .modifiedBy(pacilianId)
                .notes("Consultation requested")
                .build();
        
        historyRepository.save(history);
        
        return convertToResponseDto(savedKonsultasi);
    }

    @Override
    @Transactional
    public KonsultasiResponseDto confirmKonsultasi(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findAndValidateKonsultasi(konsultasiId, caregiverId);
        initializeState(konsultasi);
        
        String previousStatus = konsultasi.getStatus();
        
        try {
            konsultasi.confirm();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            
            KonsultasiHistory history = KonsultasiHistory.builder()
                    .konsultasiId(konsultasiId)
                    .previousStatus(previousStatus)
                    .newStatus(savedKonsultasi.getStatus())
                    .modifiedBy(caregiverId)
                    .notes("Consultation confirmed by caregiver")
                    .build();
            
            historyRepository.save(history);
            
            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto cancelKonsultasi(UUID konsultasiId, UUID userId, String role) {
        Konsultasi konsultasi = konsultasiRepository.findById(konsultasiId)
                .orElseThrow(() -> new ScheduleException("Consultation not found"));
        
        if (!"CAREGIVER".equals(role) && !"PACILIAN".equals(role)) {
            throw new ScheduleException("Invalid role");
        }
        
        if ("CAREGIVER".equals(role) && !konsultasi.getCaregiverId().equals(userId)) {
            throw new ScheduleException("You are not authorized to cancel this consultation");
        }
        
        if ("PACILIAN".equals(role) && !konsultasi.getPacilianId().equals(userId)) {
            throw new ScheduleException("You are not authorized to cancel this consultation");
        }
        
        initializeState(konsultasi);
        String previousStatus = konsultasi.getStatus();
        
        try {
            konsultasi.cancel();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            
            KonsultasiHistory history = KonsultasiHistory.builder()
                    .konsultasiId(konsultasiId)
                    .previousStatus(previousStatus)
                    .newStatus(savedKonsultasi.getStatus())
                    .modifiedBy(userId)
                    .notes("Consultation cancelled by " + role.toLowerCase())
                    .build();
            
            historyRepository.save(history);
            
            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto completeKonsultasi(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = findAndValidateKonsultasi(konsultasiId, caregiverId);
        initializeState(konsultasi);
        
        String previousStatus = konsultasi.getStatus();
        
        try {
            konsultasi.complete();
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            
            KonsultasiHistory history = KonsultasiHistory.builder()
                    .konsultasiId(konsultasiId)
                    .previousStatus(previousStatus)
                    .newStatus(savedKonsultasi.getStatus())
                    .modifiedBy(caregiverId)
                    .notes("Consultation completed")
                    .build();
            
            historyRepository.save(history);
            
            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public KonsultasiResponseDto rescheduleKonsultasi(UUID konsultasiId, RescheduleKonsultasiDto dto, UUID userId, String role) {
        Konsultasi konsultasi = konsultasiRepository.findById(konsultasiId)
                .orElseThrow(() -> new ScheduleException("Consultation not found"));
        
        if (!"CAREGIVER".equals(role) && !"PACILIAN".equals(role)) {
            throw new ScheduleException("Invalid role");
        }
        
        if ("CAREGIVER".equals(role) && !konsultasi.getCaregiverId().equals(userId)) {
            throw new ScheduleException("You are not authorized to reschedule this consultation");
        }
        
        if ("PACILIAN".equals(role) && !konsultasi.getPacilianId().equals(userId)) {
            throw new ScheduleException("You are not authorized to reschedule this consultation");
        }
        
        initializeState(konsultasi);
        String previousStatus = konsultasi.getStatus();
        
        try {
            konsultasi.reschedule(dto.getNewScheduleDateTime());
            Konsultasi savedKonsultasi = konsultasiRepository.save(konsultasi);
            
            KonsultasiHistory history = KonsultasiHistory.builder()
                    .konsultasiId(konsultasiId)
                    .previousStatus(previousStatus)
                    .newStatus(savedKonsultasi.getStatus()) 
                    .modifiedBy(userId)
                    .notes("Consultation rescheduled by " + role.toLowerCase() + ": " + dto.getNotes())
                    .build();
            
            historyRepository.save(history);
            
            return convertToResponseDto(savedKonsultasi);
        } catch (IllegalStateException e) {
            throw new ScheduleException(e.getMessage());
        }
    }

    @Override
    public KonsultasiResponseDto getKonsultasiById(UUID konsultasiId) {
        Konsultasi konsultasi = konsultasiRepository.findById(konsultasiId)
                .orElseThrow(() -> new ScheduleException("Consultation not found"));
        
        return convertToResponseDto(konsultasi);
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByPacilianId(UUID pacilianId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByPacilianId(pacilianId);
        
        return konsultasiList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<KonsultasiResponseDto> getKonsultasiByCaregiverId(UUID caregiverId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByCaregiverId(caregiverId);
        
        return konsultasiList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<KonsultasiResponseDto> getRequestedKonsultasiByCaregiverId(UUID caregiverId) {
        List<Konsultasi> konsultasiList = konsultasiRepository.findByStatusAndCaregiverId("REQUESTED", caregiverId);
        
        return konsultasiList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<KonsultasiHistoryDto> getKonsultasiHistory(UUID konsultasiId) {
        List<KonsultasiHistory> historyList = historyRepository.findByKonsultasiIdOrderByTimestampDesc(konsultasiId);
        
        return historyList.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    private Konsultasi findAndValidateKonsultasi(UUID konsultasiId, UUID caregiverId) {
        Konsultasi konsultasi = konsultasiRepository.findById(konsultasiId)
                .orElseThrow(() -> new ScheduleException("Consultation not found"));
        
        if (!konsultasi.getCaregiverId().equals(caregiverId)) {
            throw new ScheduleException("You are not authorized to modify this consultation");
        }
        
        return konsultasi;
    }

    private void initializeState(Konsultasi konsultasi) {
        switch (konsultasi.getStatus()) {
            case "REQUESTED" -> konsultasi.setState(new RequestedState());
            case "CONFIRMED" -> konsultasi.setState(new ConfirmedState());
            case "CANCELLED" -> konsultasi.setState(new CancelledState());
            case "DONE" -> konsultasi.setState(new DoneState());
            default -> throw new IllegalStateException("Unknown consultation status: " + konsultasi.getStatus());
        }
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

    private KonsultasiResponseDto convertToResponseDto(Konsultasi konsultasi) {
        return KonsultasiResponseDto.builder()
                .id(konsultasi.getId())
                .scheduleId(konsultasi.getScheduleId())
                .caregiverId(konsultasi.getCaregiverId())
                .pacilianId(konsultasi.getPacilianId())
                .scheduleDateTime(konsultasi.getScheduleDateTime())
                .notes(konsultasi.getNotes())
                .status(konsultasi.getStatus())
                .build();
    }

    private KonsultasiHistoryDto convertToHistoryDto(KonsultasiHistory history) {
        return KonsultasiHistoryDto.builder()
                .id(history.getId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .timestamp(history.getTimestamp())
                .modifiedByUserType(history.getModifiedBy().toString())
                .notes(history.getNotes())
                .build();
    }
}