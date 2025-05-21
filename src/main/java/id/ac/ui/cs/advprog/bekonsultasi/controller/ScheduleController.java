package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final TokenVerificationService tokenVerificationService;

    @PostMapping(path = "/caregiver", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ScheduleResponseDto>> createCaregiverSchedule(
            @Valid @RequestBody CreateScheduleDto scheduleDto,
            HttpServletRequest request) {

        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can create schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        ScheduleResponseDto response = scheduleService.createSchedule(scheduleDto, caregiverId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "Created successfully", response));
    }

    @PutMapping(path = "/caregiver/{scheduleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ScheduleResponseDto>> updateCaregiverSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody CreateScheduleDto scheduleDto,
            HttpServletRequest request) {

        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can update schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        ScheduleResponseDto response = scheduleService.updateSchedule(scheduleId, scheduleDto, caregiverId);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Schedule updated successfully", response));
    }

    @DeleteMapping(path = "/caregiver/{scheduleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Object>> deleteCaregiverSchedule(
            @PathVariable UUID scheduleId,
            HttpServletRequest request) {

        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can delete schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        scheduleService.deleteSchedule(scheduleId, caregiverId);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Schedule deleted successfully", null));
    }

    @GetMapping(path = "/caregiver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> getCurrentCaregiverSchedules(HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can view their schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<ScheduleResponseDto> schedules = scheduleService.getCaregiverSchedules(caregiverId);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Retrieved caregiver schedules", schedules));
    }

    @GetMapping(path = "/caregiver/{caregiverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> getCaregiverSchedulesByIdParam(
            @PathVariable UUID caregiverId,
            HttpServletRequest request) {

        verifyToken(request);
        List<ScheduleResponseDto> schedules = scheduleService.getCaregiverSchedules(caregiverId);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Retrieved caregiver schedules", schedules));
    }

    @GetMapping(path = "/{scheduleId}/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<LocalDateTime>>> getAvailableDateTimesForSchedule(
            @PathVariable UUID scheduleId,
            @RequestParam(defaultValue = "4") int weeksAhead,
            HttpServletRequest request) {

        verifyToken(request);
        List<LocalDateTime> availableTimes = scheduleService.getAvailableDateTimesForSchedule(scheduleId, weeksAhead);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Retrieved available times", availableTimes));
    }

    @GetMapping(path = "/{scheduleId}/check-availability", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Boolean>> checkScheduleAvailability(
            @PathVariable UUID scheduleId,
            @RequestParam LocalDateTime dateTime,
            HttpServletRequest request) {

        verifyToken(request);
        boolean isAvailable = scheduleService.isScheduleAvailableForDateTime(scheduleId, dateTime);

        return ResponseEntity.ok(ApiResponseDto.success(200,
                isAvailable ? "Schedule is available" : "Schedule is not available",
                isAvailable));
    }

    @PostMapping(path = "/caregiver/one-time", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ScheduleResponseDto>> createOneTimeCaregiverSchedule(
            @Valid @RequestBody CreateOneTimeScheduleDto scheduleDto,
            HttpServletRequest request) {

        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can create schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        ScheduleResponseDto response = scheduleService.createOneTimeSchedule(scheduleDto, caregiverId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "One-time schedule created successfully", response));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleScheduleConflictException(ScheduleConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleScheduleException(ScheduleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Authorization header is missing or invalid");
        }
        return authHeader.substring(7);
    }

    private void verifyToken(HttpServletRequest request) {
        String token = extractToken(request);
        tokenVerificationService.verifyToken(token);
    }
}