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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.createKonsultasi(createDto, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(201, response.getBody().getStatus());
            assertEquals(konsultasiId, response.getBody().getData().getId());
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.confirmKonsultasi(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Consultation confirmed successfully", response.getBody().getMessage());
            assertEquals("CONFIRMED", response.getBody().getData().getStatus());
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.cancelKonsultasi(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Consultation cancelled successfully", response.getBody().getMessage());
            assertEquals("CANCELLED", response.getBody().getData().getStatus());
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.completeKonsultasi(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Consultation completed successfully", response.getBody().getMessage());
            assertEquals("DONE", response.getBody().getData().getStatus());
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.rescheduleKonsultasi(konsultasiId, rescheduleDto, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Consultation rescheduled successfully", response.getBody().getMessage());
            assertEquals(rescheduleDto.getNewScheduleDateTime(), response.getBody().getData().getScheduleDateTime());
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

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.getKonsultasiById(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals(konsultasiId, response.getBody().getData().getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiById(konsultasiId);
        }

        @Test
        void testGetKonsultasiByPacilianId_Success() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            List<KonsultasiResponseDto> konsultasiList = Arrays.asList(konsultasiResponse);
            when(konsultasiService.getKonsultasiByPacilianId(any(UUID.class))).thenReturn(konsultasiList);

            ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> response =
                    konsultasiController.getKonsultasiByPacilianId(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Retrieved pacilian consultations", response.getBody().getMessage());
            assertEquals(1, response.getBody().getData().size());
            assertEquals(konsultasiId, response.getBody().getData().get(0).getId());
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

            ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> response =
                    konsultasiController.getKonsultasiByCaregiverId(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Retrieved caregiver consultations", response.getBody().getMessage());
            assertEquals(1, response.getBody().getData().size());
            assertEquals(konsultasiId, response.getBody().getData().get(0).getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getKonsultasiByCaregiverId(any(UUID.class));
        }

        @Test
        void testGetRequestedKonsultasiByCaregiverId_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            List<KonsultasiResponseDto> konsultasiList = Arrays.asList(konsultasiResponse);
            when(konsultasiService.getRequestedKonsultasiByCaregiverId(any(UUID.class)))
                    .thenReturn(konsultasiList);

            ResponseEntity<BaseResponseDto<List<KonsultasiResponseDto>>> response =
                    konsultasiController.getRequestedKonsultasiByCaregiverId(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Retrieved requested consultations", response.getBody().getMessage());
            assertEquals(1, response.getBody().getData().size());
            assertEquals(konsultasiId, response.getBody().getData().get(0).getId());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).getRequestedKonsultasiByCaregiverId(any(UUID.class));
        }
    }

    @Nested
    class RescheduleAcceptRejectTests {
        @Test
        void testAcceptReschedule_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            KonsultasiResponseDto confirmedResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .status("CONFIRMED")
                    .build();

            when(konsultasiService.acceptReschedule(eq(konsultasiId), any(UUID.class)))
                    .thenReturn(confirmedResponse);

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.acceptReschedule(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Rescheduled consultation accepted", response.getBody().getMessage());
            assertEquals("CONFIRMED", response.getBody().getData().getStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).acceptReschedule(eq(konsultasiId), any(UUID.class));
        }

        @Test
        void testAcceptReschedule_InvalidRole() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.acceptReschedule(konsultasiId, request);
            });

            assertEquals("Only caregivers can accept rescheduled consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).acceptReschedule(any(), any());
        }

        @Test
        void testRejectReschedule_Success() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            KonsultasiResponseDto requestedResponse = KonsultasiResponseDto.builder()
                    .id(konsultasiId)
                    .status("REQUESTED")
                    .build();

            when(konsultasiService.rejectReschedule(eq(konsultasiId), any(UUID.class)))
                    .thenReturn(requestedResponse);

            ResponseEntity<BaseResponseDto<KonsultasiResponseDto>> response =
                    konsultasiController.rejectReschedule(konsultasiId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Rescheduled consultation rejected", response.getBody().getMessage());
            assertEquals("REQUESTED", response.getBody().getData().getStatus());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService).rejectReschedule(eq(konsultasiId), any(UUID.class));
        }

        @Test
        void testRejectReschedule_InvalidRole() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                konsultasiController.rejectReschedule(konsultasiId, request);
            });

            assertEquals("Only caregivers can reject rescheduled consultations", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(konsultasiService, never()).rejectReschedule(any(), any());
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

            ResponseEntity<BaseResponseDto<Object>> response =
                    konsultasiController.handleScheduleException(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Schedule conflict", response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }

        @Test
        void testHandleAuthenticationException() {
            AuthenticationException exception = new AuthenticationException("Invalid token");

            ResponseEntity<BaseResponseDto<Object>> response =
                    konsultasiController.handleAuthenticationException(exception);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(401, response.getBody().getStatus());
            assertEquals("Invalid token", response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }
    }
}