package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.*;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.service.KonsultasiService;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KonsultasiControllerTest {

    @Mock
    private KonsultasiService konsultasiService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private TokenVerificationService tokenVerificationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private KonsultasiController konsultasiController;

    private UUID pacilianId;
    private UUID caregiverId;
    private UUID scheduleId;
    private UUID konsultasiId;
    private CreateKonsultasiDto createDto;
    private RescheduleKonsultasiDto rescheduleDto;
    private KonsultasiResponseDto responseDto;
    private TokenVerificationResponseDto pacilianVerification;
    private TokenVerificationResponseDto caregiverVerification;

    @BeforeEach
    void setUp() {
        pacilianId = UUID.randomUUID();
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        konsultasiId = UUID.randomUUID();

        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);

        createDto = new CreateKonsultasiDto();
        createDto.setScheduleId(scheduleId);
        createDto.setScheduleDateTime(scheduleDateTime);
        createDto.setNotes("Test notes");

        rescheduleDto = new RescheduleKonsultasiDto();
        rescheduleDto.setNewScheduleDateTime(scheduleDateTime.plusDays(7));
        rescheduleDto.setNewScheduleId(UUID.randomUUID());
        rescheduleDto.setNotes("Rescheduled");

        responseDto = KonsultasiResponseDto.builder()
                .id(konsultasiId)
                .scheduleId(scheduleId)
                .caregiverId(caregiverId)
                .pacilianId(pacilianId)
                .scheduleDateTime(scheduleDateTime)
                .notes("Test notes")
                .status("REQUESTED")
                .lastUpdated(LocalDateTime.now())
                .build();

        pacilianVerification = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(pacilianId.toString())
                .role(Role.PACILIAN)
                .build();

        caregiverVerification = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(caregiverId.toString())
                .role(Role.CAREGIVER)
                .build();
    }

    @Test
    void testCreateKonsultasi() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(konsultasiService.createKonsultasi(createDto, pacilianId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.createKonsultasi(createDto, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());
        assertEquals("Created successfully", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).createKonsultasi(createDto, pacilianId);
    }

    @Test
    void testCreateKonsultasi_NotPacilianRole() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);

        assertThrows(AuthenticationException.class, () ->
                konsultasiController.createKonsultasi(createDto, request));

        verify(konsultasiService, never()).createKonsultasi(any(), any());
    }

    @Test
    void testGetAvailableTimesForSchedule() {
        List<LocalDateTime> availableTimes = Arrays.asList(
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(14)
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(scheduleService.getAvailableDateTimesForSchedule(scheduleId, 4)).thenReturn(availableTimes);

        ResponseEntity<ApiResponseDto<List<LocalDateTime>>> response =
                konsultasiController.getAvailableTimesForSchedule(scheduleId, 4, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Retrieved available times", response.getBody().getMessage());
        assertEquals(availableTimes, response.getBody().getData());

        verify(scheduleService).getAvailableDateTimesForSchedule(scheduleId, 4);
    }

    @Test
    void testRescheduleKonsultasi() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(konsultasiService.rescheduleKonsultasi(eq(konsultasiId), eq(rescheduleDto), eq(pacilianId), eq("PACILIAN"))).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.rescheduleKonsultasi(konsultasiId, rescheduleDto, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Consultation rescheduled successfully", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).rescheduleKonsultasi(eq(konsultasiId), eq(rescheduleDto), eq(pacilianId), eq("PACILIAN"));
    }

    @Test
    void testAcceptReschedule() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.acceptReschedule(konsultasiId, caregiverId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.acceptReschedule(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Rescheduled consultation accepted", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).acceptReschedule(konsultasiId, caregiverId);
    }

    @Test
    void testRejectReschedule() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.rejectReschedule(konsultasiId, caregiverId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.rejectReschedule(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Rescheduled consultation rejected", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).rejectReschedule(konsultasiId, caregiverId);
    }

    @Test
    void testConfirmKonsultasi() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.confirmKonsultasi(konsultasiId, caregiverId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.confirmKonsultasi(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Consultation confirmed successfully", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).confirmKonsultasi(konsultasiId, caregiverId);
    }

    @Test
    void testCancelKonsultasi() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(konsultasiService.cancelKonsultasi(konsultasiId, pacilianId, "PACILIAN")).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.cancelKonsultasi(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Consultation cancelled successfully", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).cancelKonsultasi(konsultasiId, pacilianId, "PACILIAN");
    }

    @Test
    void testCompleteKonsultasi() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.completeKonsultasi(konsultasiId, caregiverId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.completeKonsultasi(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Consultation completed successfully", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).completeKonsultasi(konsultasiId, caregiverId);
    }

    @Test
    void testGetKonsultasiById() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(konsultasiService.getKonsultasiById(konsultasiId)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<KonsultasiResponseDto>> response =
                konsultasiController.getKonsultasiById(konsultasiId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());

        verify(konsultasiService).getKonsultasiById(konsultasiId);
    }

    @Test
    void testGetKonsultasiByPacilianId() {
        List<KonsultasiResponseDto> konsultasiList = Arrays.asList(responseDto);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);
        when(konsultasiService.getKonsultasiByPacilianId(pacilianId)).thenReturn(konsultasiList);

        ResponseEntity<ApiResponseDto<List<KonsultasiResponseDto>>> response =
                konsultasiController.getKonsultasiByPacilianId(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Retrieved pacilian consultations", response.getBody().getMessage());
        assertEquals(konsultasiList, response.getBody().getData());

        verify(konsultasiService).getKonsultasiByPacilianId(pacilianId);
    }

    @Test
    void testGetKonsultasiByCaregiverId() {
        List<KonsultasiResponseDto> konsultasiList = Arrays.asList(responseDto);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.getKonsultasiByCaregiverId(caregiverId)).thenReturn(konsultasiList);

        ResponseEntity<ApiResponseDto<List<KonsultasiResponseDto>>> response =
                konsultasiController.getKonsultasiByCaregiverId(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Retrieved caregiver consultations", response.getBody().getMessage());
        assertEquals(konsultasiList, response.getBody().getData());

        verify(konsultasiService).getKonsultasiByCaregiverId(caregiverId);
    }

    @Test
    void testGetRequestedKonsultasiByCaregiverId() {
        List<KonsultasiResponseDto> konsultasiList = Arrays.asList(responseDto);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(caregiverVerification);
        when(konsultasiService.getRequestedKonsultasiByCaregiverId(caregiverId)).thenReturn(konsultasiList);

        ResponseEntity<ApiResponseDto<List<KonsultasiResponseDto>>> response =
                konsultasiController.getRequestedKonsultasiByCaregiverId(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Retrieved requested consultations", response.getBody().getMessage());
        assertEquals(konsultasiList, response.getBody().getData());

        verify(konsultasiService).getRequestedKonsultasiByCaregiverId(caregiverId);
    }
}