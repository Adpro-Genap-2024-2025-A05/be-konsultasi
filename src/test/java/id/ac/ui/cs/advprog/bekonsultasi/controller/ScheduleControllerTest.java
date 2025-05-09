package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private TokenVerificationService tokenVerificationService;

    private ScheduleController scheduleController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduleController = new ScheduleController(scheduleService, tokenVerificationService);
    }

    @Test
    void createCaregiverSchedule_shouldReturnCreatedSchedule() {
        CreateScheduleDto dto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        UUID caregiverId = UUID.randomUUID();

        TokenVerificationResponseDto tokenDto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(caregiverId.toString())
                .role(Role.CAREGIVER)
                .build();

        ScheduleResponseDto expectedResponse = ScheduleResponseDto.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        when(tokenVerificationService.verifyToken("validToken")).thenReturn(tokenDto);
        when(scheduleService.createSchedule(eq(dto), eq(caregiverId))).thenReturn(expectedResponse);

        ResponseEntity<ScheduleResponseDto> response = scheduleController.createCaregiverSchedule(dto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(tokenVerificationService).verifyToken("validToken");
        verify(scheduleService).createSchedule(eq(dto), eq(caregiverId));
    }

    @Test
    void createCaregiverSchedule_shouldThrowExceptionForNonCaregivers() {
        CreateScheduleDto dto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        TokenVerificationResponseDto tokenDto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(UUID.randomUUID().toString())
                .role(Role.PACILIAN)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        when(tokenVerificationService.verifyToken("validToken")).thenReturn(tokenDto);

        assertThrows(AuthenticationException.class, () -> {
            scheduleController.createCaregiverSchedule(dto, request);
        });

        verify(tokenVerificationService).verifyToken("validToken");
        verify(scheduleService, never()).createSchedule(any(), any());
    }

    @Test
    void getCurrentCaregiverSchedules_shouldReturnCaregiverSchedules() {
        UUID caregiverId = UUID.randomUUID();

        TokenVerificationResponseDto tokenDto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(caregiverId.toString())
                .role(Role.CAREGIVER)
                .build();

        List<ScheduleResponseDto> expectedSchedules = Arrays.asList(
                ScheduleResponseDto.builder()
                        .id(UUID.randomUUID())
                        .caregiverId(caregiverId)
                        .day(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .status("AVAILABLE")
                        .build(),
                ScheduleResponseDto.builder()
                        .id(UUID.randomUUID())
                        .caregiverId(caregiverId)
                        .day(DayOfWeek.TUESDAY)
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(15, 0))
                        .status("AVAILABLE")
                        .build()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        when(tokenVerificationService.verifyToken("validToken")).thenReturn(tokenDto);
        when(scheduleService.getCaregiverSchedules(caregiverId)).thenReturn(expectedSchedules);

        ResponseEntity<List<ScheduleResponseDto>> response = scheduleController.getCurrentCaregiverSchedules(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchedules, response.getBody());
        verify(tokenVerificationService).verifyToken("validToken");
        verify(scheduleService).getCaregiverSchedules(caregiverId);
    }

    @Test
    void getCurrentCaregiverSchedules_shouldThrowExceptionForNonCaregivers() {
        TokenVerificationResponseDto tokenDto = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(UUID.randomUUID().toString())
                .role(Role.PACILIAN)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        when(tokenVerificationService.verifyToken("validToken")).thenReturn(tokenDto);

        assertThrows(AuthenticationException.class, () -> {
            scheduleController.getCurrentCaregiverSchedules(request);
        });

        verify(tokenVerificationService).verifyToken("validToken");
        verify(scheduleService, never()).getCaregiverSchedules(any());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        String errorMessage = "Test error message";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        ResponseEntity<Map<String, String>> response = scheduleController.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("error"));
    }

    @Test
    void handleScheduleConflictException_shouldReturnConflict() {
        String errorMessage = "Test conflict message";
        ScheduleConflictException ex = new ScheduleConflictException(errorMessage);

        ResponseEntity<Map<String, String>> response = scheduleController.handleScheduleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("error"));
        assertEquals("SCHEDULE_CONFLICT", response.getBody().get("errorType"));
    }

    @Test
    void handleAuthenticationException_shouldReturnUnauthorized() {
        String errorMessage = "Test auth message";
        AuthenticationException ex = new AuthenticationException(errorMessage);

        ResponseEntity<Map<String, String>> response = scheduleController.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("error"));
    }

    @Test
    void extractToken_shouldThrowExceptionForMissingAuthHeader() {
        MockHttpServletRequest invalidRequest = new MockHttpServletRequest();

        assertThrows(AuthenticationException.class, () -> {
            scheduleController.getCurrentCaregiverSchedules(invalidRequest);
        });
    }

    @Test
    void extractToken_shouldThrowExceptionForInvalidAuthHeader() {
        MockHttpServletRequest invalidRequest = new MockHttpServletRequest();
        invalidRequest.addHeader("Authorization", "InvalidHeader");

        assertThrows(AuthenticationException.class, () -> {
            scheduleController.getCurrentCaregiverSchedules(invalidRequest);
        });
    }
}
