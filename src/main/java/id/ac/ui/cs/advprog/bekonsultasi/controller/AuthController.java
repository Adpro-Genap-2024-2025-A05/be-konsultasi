package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final TokenVerificationService tokenVerificationService;

    @PostMapping(path = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> verifyToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error(HttpStatus.UNAUTHORIZED.value(), 
                                        "Invalid authentication token"));
        }

        String token = authHeader.substring(7);
        TokenVerificationResponseDto response = tokenVerificationService.verifyToken(token);

        if (response.isValid()) {
            return ResponseEntity.ok(
                    ApiResponseDto.success(HttpStatus.OK.value(), 
                                    "Token verified successfully", 
                                    response));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error(HttpStatus.UNAUTHORIZED.value(), 
                                        "Invalid or expired token"));
        }
    }
}