package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.service.KonsultasiService;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class KonsultasiControllerTest {

    @Mock
    private KonsultasiService konsultasiService;
    
    @Mock
    private TokenVerificationService tokenVerificationService;
    
    @InjectMocks
    private KonsultasiController konsultasiController;
    
    private UUID userId;
    private UUID konsultasiId;
    private String token;
    private MockHttpServletRequest request;
    private TokenVerificationResponseDto tokenResponse;
    private KonsultasiResponseDto konsultasiResponse;

    @BeforeEach
    void setUp() {
        openMocks(this);
        
        userId = UUID.randomUUID();
        konsultasiId = UUID.randomUUID();
        token = "test-token";
        
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        
        tokenResponse = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(userId.toString())
                .email("test@example.com")
                .role(Role.CAREGIVER)
                .expiresIn(3600L)
                .build();
        
        konsultasiResponse = KonsultasiResponseDto.builder()
                .id(konsultasiId)
                .caregiverId(userId)
                .pacilianId(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .scheduleDateTime(LocalDateTime.now().plusDays(7))
                .notes("Test notes")
                .status("REQUESTED")
                .build();
    }

    @Nested
    class CreateKonsultasiTests {
        @Test
        void testCreateKonsultasi_Success() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            CreateKonsultasiDto createDto = CreateKonsultasiDto.builder()
                    .scheduleId(UUID.randomUUID())
                    .notes("Test consultation")
                    .build();
            
            when(konsultasiService.createKonsultasi(eq(createDto), any(UUID.class)))
                    .thenReturn(konsultasiResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.createKonsultasi(createDto, request);
            
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(konsultasiId, response.getBody().getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).createKonsultasi(eq(createDto), any(UUID.class));
        }
        
        @Test
        void testCreateKonsultasi_InvalidRole() { 
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            CreateKonsultasiDto createDto = CreateKonsultasiDto.builder()
                    .scheduleId(UUID.randomUUID())
                    .notes("Test consultation")
                    .build();
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.createKonsultasi(createDto, request);
            });
            
            assertEquals("Only pacilians can create consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).createKonsultasi(any(), any());
        }
    }
    
    @Nested
    class ConfirmKonsultasiTests {
        @Test
        void testConfirmKonsultasi_Success() { 
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            KonsultasiResponseDto confirmedResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .status("CONFIRMED")
                    .build();
            
            when(konsultasiService.confirmKonsultasi(eq(konsultasiId), any(UUID.class)))
                    .thenReturn(confirmedResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.confirmKonsultasi(konsultasiId, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("CONFIRMED", response.getBody().getStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).confirmKonsultasi(eq(konsultasiId), any(UUID.class));
        }
        
        @Test
        void testConfirmKonsultasi_InvalidRole() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.confirmKonsultasi(konsultasiId, request);
            });
            
            assertEquals("Only caregivers can confirm consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).confirmKonsultasi(any(), any());
        }
    }
    
    @Nested
    class CancelAndCompleteTests {
        @Test
        void testCancelKonsultasi_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            KonsultasiResponseDto cancelledResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .status("CANCELLED")
                    .build();
            
            when(konsultasiService.cancelKonsultasi(eq(konsultasiId), any(UUID.class), eq("CAREGIVER")))
                    .thenReturn(cancelledResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.cancelKonsultasi(konsultasiId, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("CANCELLED", response.getBody().getStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).cancelKonsultasi(eq(konsultasiId), any(UUID.class), eq("CAREGIVER"));
        }
        
        @Test
        void testCompleteKonsultasi_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            KonsultasiResponseDto completedResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .status("DONE")
                    .build();
            
            when(konsultasiService.completeKonsultasi(eq(konsultasiId), any(UUID.class)))
                    .thenReturn(completedResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.completeKonsultasi(konsultasiId, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("DONE", response.getBody().getStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).completeKonsultasi(eq(konsultasiId), any(UUID.class));
        }
        
        @Test
        void testCompleteKonsultasi_InvalidRole() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.completeKonsultasi(konsultasiId, request);
            });
            
            assertEquals("Only caregivers can complete consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).completeKonsultasi(any(), any());
        }
    }
    
    @Nested
    class RescheduleTests {
        @Test
        void testRescheduleKonsultasi_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            RescheduleKonsultasiDto rescheduleDto = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(LocalDateTime.now().plusDays(14))
                    .notes("Reschedule notes")
                    .build();
            
            KonsultasiResponseDto rescheduledResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .scheduleDateTime(rescheduleDto.getNewScheduleDateTime())
                    .status("REQUESTED")
                    .build();
            
            when(konsultasiService.rescheduleKonsultasi(
                    eq(konsultasiId), eq(rescheduleDto), any(UUID.class), eq("CAREGIVER")))
                    .thenReturn(rescheduledResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.rescheduleKonsultasi(konsultasiId, rescheduleDto, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(rescheduleDto.getNewScheduleDateTime(), response.getBody().getScheduleDateTime());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).rescheduleKonsultasi(
                    eq(konsultasiId), eq(rescheduleDto), any(UUID.class), eq("CAREGIVER"));
        }
    }
    
    @Nested
    class QueryKonsultasiTests {
        @Test
        void testGetKonsultasiById_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            when(konsultasiService.getKonsultasiById(konsultasiId)).thenReturn(konsultasiResponse);
            
            ResponseEntity<KonsultasiResponseDto> response = 
                    konsultasiController.getKonsultasiById(konsultasiId, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(konsultasiId, response.getBody().getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiById(konsultasiId);
        }
        
        @Test
        void testGetKonsultasiByPacilianId_Success() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            List<KonsultasiResponseDto> konsultasiList = Arrays.asList(konsultasiResponse);
            when(konsultasiService.getKonsultasiByPacilianId(any(UUID.class))).thenReturn(konsultasiList);
            
            ResponseEntity<List<KonsultasiResponseDto>> response = 
                    konsultasiController.getKonsultasiByPacilianId(request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals(konsultasiId, response.getBody().get(0).getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiByPacilianId(any(UUID.class));
        }
        
        @Test
        void testGetKonsultasiByPacilianId_InvalidRole() { 
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.getKonsultasiByPacilianId(request);
            });
            
            assertEquals("Only pacilians can view their consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).getKonsultasiByPacilianId(any());
        }
        
        @Test
        void testGetKonsultasiByCaregiverId_Success() { 
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            List<KonsultasiResponseDto> konsultasiList = Arrays.asList(konsultasiResponse);
            when(konsultasiService.getKonsultasiByCaregiverId(any(UUID.class))).thenReturn(konsultasiList);
            
            ResponseEntity<List<KonsultasiResponseDto>> response = 
                    konsultasiController.getKonsultasiByCaregiverId(request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals(konsultasiId, response.getBody().get(0).getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiByCaregiverId(any(UUID.class));
        }
        
        @Test
        void testGetRequestedKonsultasiByCaregiverId_Success() { 
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            List<KonsultasiResponseDto> konsultasiList = Arrays.asList(konsultasiResponse);
            when(konsultasiService.getRequestedKonsultasiByCaregiverId(any(UUID.class)))
                    .thenReturn(konsultasiList);
            
            ResponseEntity<List<KonsultasiResponseDto>> response = 
                    konsultasiController.getRequestedKonsultasiByCaregiverId(request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals(konsultasiId, response.getBody().get(0).getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getRequestedKonsultasiByCaregiverId(any(UUID.class));
        }
        
        @Test
        void testGetKonsultasiHistory_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            
            KonsultasiHistoryDto historyDto = KonsultasiHistoryDto.builder()
                    .id(UUID.randomUUID())
                    .previousStatus("REQUESTED")
                    .newStatus("CONFIRMED")
                    .timestamp(LocalDateTime.now())
                    .modifiedByUserType(userId.toString())
                    .notes("Test history")
                    .build();
            
            List<KonsultasiHistoryDto> historyList = Arrays.asList(historyDto);
            when(konsultasiService.getKonsultasiHistory(konsultasiId)).thenReturn(historyList);
            
            ResponseEntity<List<KonsultasiHistoryDto>> response = 
                    konsultasiController.getKonsultasiHistory(konsultasiId, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals("REQUESTED", response.getBody().get(0).getPreviousStatus());
            assertEquals("CONFIRMED", response.getBody().get(0).getNewStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiHistory(konsultasiId);
        }
    }
    
    @Nested
    class AuthenticationTests {
        @Test
        void testMissingAuthorizationHeader() {
            MockHttpServletRequest requestWithoutHeader = new MockHttpServletRequest();
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.getKonsultasiById(konsultasiId, requestWithoutHeader);
            });
            
            assertEquals("Authorization header is missing or invalid", exception.getMessage());
            verify(tokenVerificationService, never()).verifyToken(any());
        }
    }
    
    @Nested
    class ExceptionHandlerTests {
        @Test
        void testHandleScheduleException() {
            ScheduleException exception = new ScheduleException("Schedule conflict");
            
            ResponseEntity<Map<String, String>> response = 
                    konsultasiController.handleScheduleException(exception);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Schedule conflict", response.getBody().get("error"));
        }
        
        @Test
        void testHandleAuthenticationException() {
            AuthenticationException exception = new AuthenticationException("Invalid token");
            
            ResponseEntity<Map<String, String>> response = 
                    konsultasiController.handleAuthenticationException(exception);
            
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid token", response.getBody().get("error"));
        }
    }
}