package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private TokenVerificationService tokenVerificationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ScheduleController scheduleController;

    private UUID caregiverId;
    private UUID scheduleId;
    private TokenVerificationResponseDto verificationResponseDto;
    private ScheduleResponseDto scheduleResponseDto;
    private ScheduleResponseDto oneTimeScheduleResponseDto;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        verificationResponseDto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(caregiverId.toString())
                .role(Role.CAREGIVER)
                .build();

        scheduleResponseDto = ScheduleResponseDto.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .oneTime(false)
                .build();

        oneTimeScheduleResponseDto = ScheduleResponseDto.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .specificDate(LocalDate.of(2025, 6, 2))
                .oneTime(true)
                .build();
    }

    @Test
    void testCreateOneTimeCaregiverSchedule() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        dto.setSpecificDate(LocalDate.of(2025, 6, 2));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(verificationResponseDto);
        when(scheduleService.createOneTimeSchedule(dto, caregiverId)).thenReturn(oneTimeScheduleResponseDto);

        ResponseEntity<ApiResponseDto<ScheduleResponseDto>> response =
                scheduleController.createOneTimeCaregiverSchedule(dto, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());
        assertEquals("One-time schedule created successfully", response.getBody().getMessage());
        assertEquals(oneTimeScheduleResponseDto, response.getBody().getData());

        verify(scheduleService).createOneTimeSchedule(dto, caregiverId);
    }

    @Test
    void testCreateOneTimeCaregiverSchedule_NotCaregiverRole() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        dto.setSpecificDate(LocalDate.of(2025, 6, 2));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        TokenVerificationResponseDto pacilianVerification = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(UUID.randomUUID().toString())
                .role(Role.PACILIAN)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(pacilianVerification);

        assertThrows(AuthenticationException.class, () ->
                scheduleController.createOneTimeCaregiverSchedule(dto, request));

        verify(scheduleService, never()).createOneTimeSchedule(any(), any());
    }

    @Test
    void testGetAvailableDateTimesForSchedule() {
        List<LocalDateTime> availableTimes = Arrays.asList(
                LocalDateTime.of(2025, 6, 2, 10, 0),
                LocalDateTime.of(2025, 6, 9, 10, 0)
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(verificationResponseDto);
        when(scheduleService.getAvailableDateTimesForSchedule(scheduleId, 4)).thenReturn(availableTimes);

        ResponseEntity<ApiResponseDto<List<LocalDateTime>>> response =
                scheduleController.getAvailableDateTimesForSchedule(scheduleId, 4, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Retrieved available times", response.getBody().getMessage());
        assertEquals(availableTimes, response.getBody().getData());

        verify(scheduleService).getAvailableDateTimesForSchedule(scheduleId, 4);
    }

    @Test
    void testCheckScheduleAvailability() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 2, 10, 0);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(verificationResponseDto);
        when(scheduleService.isScheduleAvailableForDateTime(scheduleId, dateTime)).thenReturn(true);

        ResponseEntity<ApiResponseDto<Boolean>> response =
                scheduleController.checkScheduleAvailability(scheduleId, dateTime, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Schedule is available", response.getBody().getMessage());
        assertTrue(response.getBody().getData());

        verify(scheduleService).isScheduleAvailableForDateTime(scheduleId, dateTime);
    }

    @Test
    void testCheckScheduleAvailability_NotAvailable() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 2, 10, 0);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenVerificationService.verifyToken("token")).thenReturn(verificationResponseDto);
        when(scheduleService.isScheduleAvailableForDateTime(scheduleId, dateTime)).thenReturn(false);

        ResponseEntity<ApiResponseDto<Boolean>> response =
                scheduleController.checkScheduleAvailability(scheduleId, dateTime, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Schedule is not available", response.getBody().getMessage());
        assertFalse(response.getBody().getData());

        verify(scheduleService).isScheduleAvailableForDateTime(scheduleId, dateTime);
    }
}