package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.service.KonsultasiService;
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
@RequestMapping("/konsultasi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KonsultasiController {
    private final KonsultasiService konsultasiService;
    private final TokenVerificationService tokenVerificationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> createKonsultasi(
            @Valid @RequestBody CreateKonsultasiDto dto,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.PACILIAN, "Only pacilians can create consultations");
        UUID pacilianId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.createKonsultasi(dto, pacilianId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponseDto.created(response));
    }

    @PostMapping(path = "/{konsultasiId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> confirmKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can confirm consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.confirmKonsultasi(konsultasiId, caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Consultation confirmed successfully"));
    }

    @PostMapping(path = "/{konsultasiId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> cancelKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyToken(request);
        UUID userId = UUID.fromString(verification.getUserId());
        String role = verification.getRole().name();

        KonsultasiResponseDto response = konsultasiService.cancelKonsultasi(konsultasiId, userId, role);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Consultation cancelled successfully"));
    }

    @PostMapping(path = "/{konsultasiId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> completeKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can complete consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.completeKonsultasi(konsultasiId, caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Consultation completed successfully"));
    }

    @PostMapping(path = "/{konsultasiId}/accept-reschedule", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> acceptReschedule(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can accept rescheduled consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.acceptReschedule(konsultasiId, caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Rescheduled consultation accepted"));
    }

    @PostMapping(path = "/{konsultasiId}/reject-reschedule", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> rejectReschedule(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can reject rescheduled consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.rejectReschedule(konsultasiId, caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Rescheduled consultation rejected"));
    }

    @PostMapping(path = "/{konsultasiId}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> rescheduleKonsultasi(
            @PathVariable UUID konsultasiId,
            @Valid @RequestBody RescheduleKonsultasiDto dto,
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyToken(request);
        UUID userId = UUID.fromString(verification.getUserId());
        String role = verification.getRole().name();

        KonsultasiResponseDto response = konsultasiService.rescheduleKonsultasi(konsultasiId, dto, userId, role);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Consultation rescheduled successfully"));
    }

    @GetMapping(path = "/{konsultasiId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> getKonsultasiById(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        verifyToken(request);
        KonsultasiResponseDto response = konsultasiService.getKonsultasiById(konsultasiId);

        return ResponseEntity.ok(BaseResponseDto.success(response));
    }

    @GetMapping(path = "/pacilian", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> getKonsultasiByPacilianId(
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.PACILIAN, "Only pacilians can view their consultations");
        UUID pacilianId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getKonsultasiByPacilianId(pacilianId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Retrieved pacilian consultations"));
    }

    @GetMapping(path = "/caregiver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> getKonsultasiByCaregiverId(
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can view their consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getKonsultasiByCaregiverId(caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Retrieved caregiver consultations"));
    }

    @GetMapping(path = "/caregiver/requested", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> getRequestedKonsultasiByCaregiverId(
            HttpServletRequest request) {
        TokenVerificationResponseDto verification = verifyTokenAndRole(request, Role.CAREGIVER, "Only caregivers can view their requested consultations");
        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getRequestedKonsultasiByCaregiverId(caregiverId);

        return ResponseEntity.ok(BaseResponseDto.success(response, "Retrieved requested consultations"));
    }

    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleScheduleException(ScheduleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponseDto.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Authorization header is missing or invalid");
        }
        return authHeader.substring(7);
    }

    private TokenVerificationResponseDto verifyToken(HttpServletRequest request) {
        String token = extractToken(request);
        return tokenVerificationService.verifyToken(token);
    }
    
    private TokenVerificationResponseDto verifyTokenAndRole(HttpServletRequest request, Role requiredRole, String errorMessage) {
        TokenVerificationResponseDto verification = verifyToken(request);
        if (verification.getRole() != requiredRole) {
            throw new AuthenticationException(errorMessage);
        }
        return verification;
    }
}