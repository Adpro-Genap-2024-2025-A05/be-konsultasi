package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class TokenVerificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TokenVerificationService tokenVerificationService;

    private final String authServiceUrl = "http://localhost:8080/auth";
    private final String token = "test-token";
    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        openMocks(this);
        ReflectionTestUtils.setField(tokenVerificationService, "authServiceUrl", authServiceUrl);
    }

    @Nested
    class VerifyTokenTests {
        @Test
        void testVerifyToken_Success() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            TokenVerificationResponseDto result = tokenVerificationService.verifyToken(token);

            assertNotNull(result);
            assertTrue(result.isValid());
            assertEquals(userId, result.getUserId());
            assertEquals("test@example.com", result.getEmail());
            assertEquals(Role.CAREGIVER, result.getRole());
            assertEquals(3600L, result.getExpiresIn());
        }

        @Test
        void testVerifyToken_InvalidToken() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(false)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });

            assertEquals("Error verifying token: Invalid or expired token", exception.getMessage());
        }

        @Test
        void testVerifyToken_RestTemplateException() {
            when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenThrow(new RuntimeException("Connection error"));

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });

            assertTrue(exception.getMessage().contains("Error verifying token"));
        }
    }

    @Nested
    class TokenInformationExtractionTests {
        @Test
        void testGetUserIdFromToken() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            UUID result = tokenVerificationService.getUserIdFromToken(token);

            assertEquals(UUID.fromString(userId), result);
        }

        @Test
        void testGetUserRoleFromToken() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email("test@example.com")
                    .role(Role.PACILIAN)
                    .expiresIn(3600L)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            Role result = tokenVerificationService.getUserRoleFromToken(token);

            assertEquals(Role.PACILIAN, result);
        }
    }

    @Nested
    class RoleValidationTests {
        @Test
        void testValidateRole_Success() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            tokenVerificationService.validateRole(token, Role.CAREGIVER);
        }

        @Test
        void testValidateRole_WrongRole() {
            TokenVerificationResponseDto responseDto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email("test@example.com")
                    .role(Role.PACILIAN)
                    .expiresIn(3600L)
                    .build();

            ResponseEntity<TokenVerificationResponseDto> responseEntity = 
                    new ResponseEntity<>(responseDto, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(authServiceUrl + "/verify"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(TokenVerificationResponseDto.class)
            )).thenReturn(responseEntity);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.validateRole(token, Role.CAREGIVER);
            });

            assertEquals("Access denied. Required role: CAREGIVER", exception.getMessage());
        }
    }
}