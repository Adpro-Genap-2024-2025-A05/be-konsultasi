package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private MockHttpServletRequest request;
    private CreateScheduleDto createScheduleDto;
    private ScheduleResponseDto scheduleResponseDto;
    private final UUID caregiverId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        createScheduleDto = new CreateScheduleDto();
        createScheduleDto.setDay(DayOfWeek.MONDAY);
        createScheduleDto.setStartTime(LocalTime.of(9, 0));
        createScheduleDto.setEndTime(LocalTime.of(10, 0));

        scheduleResponseDto = ScheduleResponseDto.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<Map<String, String>> response = scheduleController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("Consultation Schedule API", response.getBody().get("service"));
    }

    @Test
    void testCreateCaregiverScheduleSuccess() {
        when(scheduleService.createSchedule(any(), any())).thenReturn(scheduleResponseDto);

        ResponseEntity<ScheduleResponseDto> response =
                scheduleController.createCaregiverSchedule(createScheduleDto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(scheduleResponseDto, response.getBody());
        verify(scheduleService).createSchedule(createScheduleDto, caregiverId);
    }

    @Test
    void testCreateCaregiverScheduleUnauthorized() {
        request.removeHeader("Authorization");

        ResponseEntity<ScheduleResponseDto> response =
                scheduleController.createCaregiverSchedule(createScheduleDto, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetCaregiverSchedulesSuccess() {
        when(scheduleService.getCaregiverSchedules(caregiverId))
                .thenReturn(List.of(scheduleResponseDto));

        ResponseEntity<List<ScheduleResponseDto>> response =
                scheduleController.getCaregiverSchedules(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(scheduleResponseDto, response.getBody().get(0));
    }

    @Test
    void testGetCaregiverSchedulesUnauthorized() {
        request.removeHeader("Authorization");

        ResponseEntity<List<ScheduleResponseDto>> response =
                scheduleController.getCaregiverSchedules(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid time");

        ResponseEntity<Map<String, String>> response =
                scheduleController.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid time", response.getBody().get("error"));
    }

    @Test
    void testHandleScheduleConflictException() {
        ScheduleConflictException ex = new ScheduleConflictException("Conflict found");

        ResponseEntity<Map<String, String>> response =
                scheduleController.handleScheduleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict found", response.getBody().get("error"));
        assertEquals("SCHEDULE_CONFLICT", response.getBody().get("errorType"));
    }

    @Test
    void testCreateCaregiverScheduleInvalidInput() {
        createScheduleDto.setDay(null);

        ResponseEntity<ScheduleResponseDto> response =
                scheduleController.createCaregiverSchedule(createScheduleDto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(scheduleService).createSchedule(createScheduleDto, caregiverId);
    }
}