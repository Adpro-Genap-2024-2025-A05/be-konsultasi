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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/konsultasi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KonsultasiController {
    private final KonsultasiService konsultasiService;
    private final TokenVerificationService tokenVerificationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> createKonsultasi(
            @Valid @RequestBody CreateKonsultasiDto dto,
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.PACILIAN) {
            throw new AuthenticationException("Only pacilians can create consultations");
        }
        
        UUID pacilianId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.createKonsultasi(dto, pacilianId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{konsultasiId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> confirmKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can confirm consultations");
        }
        
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.confirmKonsultasi(konsultasiId, caregiverId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/{konsultasiId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> cancelKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        UUID userId = UUID.fromString(verification.getUserId());
        String role = verification.getRole().name();
        
        KonsultasiResponseDto response = konsultasiService.cancelKonsultasi(konsultasiId, userId, role);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/{konsultasiId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> completeKonsultasi(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can complete consultations");
        }
        
        UUID caregiverId = UUID.fromString(verification.getUserId());
        KonsultasiResponseDto response = konsultasiService.completeKonsultasi(konsultasiId, caregiverId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/{konsultasiId}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> rescheduleKonsultasi(
            @PathVariable UUID konsultasiId,
            @Valid @RequestBody RescheduleKonsultasiDto dto,
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        UUID userId = UUID.fromString(verification.getUserId());
        String role = verification.getRole().name();
        
        KonsultasiResponseDto response = konsultasiService.rescheduleKonsultasi(konsultasiId, dto, userId, role);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{konsultasiId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KonsultasiResponseDto> getKonsultasiById(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        verifyToken(request);
        
        KonsultasiResponseDto response = konsultasiService.getKonsultasiById(konsultasiId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/pacilian", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KonsultasiResponseDto>> getKonsultasiByPacilianId(
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.PACILIAN) {
            throw new AuthenticationException("Only pacilians can view their consultations");
        }
        
        UUID pacilianId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getKonsultasiByPacilianId(pacilianId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/caregiver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KonsultasiResponseDto>> getKonsultasiByCaregiverId(
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can view their consultations");
        }
        
        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getKonsultasiByCaregiverId(caregiverId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/caregiver/requested", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KonsultasiResponseDto>> getRequestedKonsultasiByCaregiverId(
            HttpServletRequest request) {
        String token = extractToken(request);
        TokenVerificationResponseDto verification = tokenVerificationService.verifyToken(token);
        
        if (verification.getRole() != Role.CAREGIVER) {
            throw new AuthenticationException("Only caregivers can view their requested consultations");
        }
        
        UUID caregiverId = UUID.fromString(verification.getUserId());
        List<KonsultasiResponseDto> response = konsultasiService.getRequestedKonsultasiByCaregiverId(caregiverId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{konsultasiId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KonsultasiHistoryDto>> getKonsultasiHistory(
            @PathVariable UUID konsultasiId,
            HttpServletRequest request) {
        verifyToken(request);
        
        List<KonsultasiHistoryDto> response = konsultasiService.getKonsultasiHistory(konsultasiId);
        
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<Map<String, String>> handleScheduleException(ScheduleException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
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