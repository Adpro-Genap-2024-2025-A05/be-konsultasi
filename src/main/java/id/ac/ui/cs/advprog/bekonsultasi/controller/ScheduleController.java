package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.BaseResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
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
    public ResponseEntity<BaseResponseDto<ScheduleResponseDto>> createCaregiverSchedule(
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
                .body(BaseResponseDto.created(response));
    }

    @PutMapping(path = "/caregiver/{scheduleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<ScheduleResponseDto>> updateCaregiverSchedule(
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

        return ResponseEntity.ok(BaseResponseDto.success(response, "Schedule updated successfully"));
    }

    @DeleteMapping(path = "/caregiver/{scheduleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<Object>> deleteCaregiverSchedule(
            @PathVariable UUID scheduleId,
            HttpServletRequest request) {

        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can delete schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        scheduleService.deleteSchedule(scheduleId, caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(null, "Schedule deleted successfully"));
    }

    @GetMapping(path = "/caregiver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<ScheduleResponseDto>>> getCurrentCaregiverSchedules(HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);

        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can view their schedules");
        }

        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<ScheduleResponseDto> schedules = scheduleService.getCaregiverSchedules(caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(schedules, "Retrieved caregiver schedules"));
    }

    @GetMapping(path = "/caregiver/{caregiverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<ScheduleResponseDto>>> getCaregiverSchedulesByIdParam(
            @PathVariable UUID caregiverId,
            HttpServletRequest request) {

        verifyToken(request);
        List<ScheduleResponseDto> schedules = scheduleService.getCaregiverSchedules(caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(schedules, "Retrieved caregiver schedules"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleScheduleConflictException(ScheduleConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponseDto.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponseDto.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleScheduleException(ScheduleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
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