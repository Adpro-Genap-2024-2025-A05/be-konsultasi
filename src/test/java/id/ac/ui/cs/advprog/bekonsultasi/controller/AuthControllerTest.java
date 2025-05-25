package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TokenVerificationService tokenVerificationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    private TokenVerificationResponseDto validTokenResponse;
    private TokenVerificationResponseDto invalidTokenResponse;
    private final String validToken = "valid-jwt-token";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validTokenResponse = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(userId.toString())
                .email("test@example.com")
                .userName("Test User")
                .role(Role.PACILIAN)
                .expiresIn(3600L)
                .build();

        invalidTokenResponse = TokenVerificationResponseDto.builder()
                .valid(false)
                .userId(null)
                .email(null)
                .userName(null)
                .role(null)
                .expiresIn(0L)
                .build();
    }

    @Test
    void testVerifyToken_ValidToken_Success() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenVerificationService.verifyToken(validToken)).thenReturn(validTokenResponse);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Token verified successfully", response.getBody().getMessage());
        assertEquals(validTokenResponse, response.getBody().getData());

        verify(tokenVerificationService).verifyToken(validToken);
    }

    @Test
    void testVerifyToken_InvalidToken_ReturnsFalse() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenVerificationService.verifyToken(validToken)).thenReturn(invalidTokenResponse);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid or expired token", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(tokenVerificationService).verifyToken(validToken);
    }

    @Test
    void testVerifyToken_MissingAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid authentication token", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(tokenVerificationService, never()).verifyToken(any());
    }

    @Test
    void testVerifyToken_InvalidAuthorizationHeaderFormat() {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + validToken);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid authentication token", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(tokenVerificationService, never()).verifyToken(any());
    }

    @Test
    void testVerifyToken_EmptyAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn("");

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid authentication token", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(tokenVerificationService, never()).verifyToken(any());
    }

    @Test
    void testVerifyToken_OnlyBearerWithoutToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid authentication token", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(tokenVerificationService, never()).verifyToken(any());
    }

    @Test
    void testVerifyToken_BearerWithSpaceButNoToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(tokenVerificationService.verifyToken(""))
                .thenThrow(new AuthenticationException("Invalid token"));

        assertThrows(AuthenticationException.class, () -> {
            authController.verifyToken(request);
        });

        verify(tokenVerificationService).verifyToken("");
    }

    @Test
    void testVerifyToken_ServiceThrowsAuthenticationException() {
        String invalidToken = "expired-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(tokenVerificationService.verifyToken(invalidToken))
                .thenThrow(new AuthenticationException("Token has expired"));

        assertThrows(AuthenticationException.class, () -> {
            authController.verifyToken(request);
        });

        verify(tokenVerificationService).verifyToken(invalidToken);
    }

    @Test
    void testVerifyToken_ServiceThrowsRuntimeException() {
        String problematicToken = "problematic-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + problematicToken);
        when(tokenVerificationService.verifyToken(problematicToken))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> {
            authController.verifyToken(request);
        });

        verify(tokenVerificationService).verifyToken(problematicToken);
    }

    @Test
    void testVerifyToken_ValidTokenWithCaregiverRole() {
        TokenVerificationResponseDto caregiverTokenResponse = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(userId.toString())
                .email("caregiver@example.com")
                .userName("Dr. Test")
                .role(Role.CAREGIVER)
                .expiresIn(7200L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenVerificationService.verifyToken(validToken)).thenReturn(caregiverTokenResponse);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Token verified successfully", response.getBody().getMessage());
        assertEquals(caregiverTokenResponse, response.getBody().getData());
        assertEquals(Role.CAREGIVER, response.getBody().getData().getRole());

        verify(tokenVerificationService).verifyToken(validToken);
    }

    @Test
    void testVerifyToken_TokenExtractionEdgeCase() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(tokenVerificationService.verifyToken("")).thenReturn(validTokenResponse);

        ResponseEntity<ApiResponseDto<TokenVerificationResponseDto>> response =
                authController.verifyToken(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(tokenVerificationService).verifyToken("");
    }
}