package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import io.micrometer.core.instrument.Counter;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private Counter availableSchedulesRequestCounter;

    @InjectMocks
    private DataController dataController;

    private UUID caregiverId;
    private UUID caregiverId2;
    private ScheduleResponseDto scheduleDto1;
    private ScheduleResponseDto scheduleDto2;
    private List<ScheduleResponseDto> scheduleList;

    @BeforeEach
    void setUp() {
        caregiverId = UUID.randomUUID();
        caregiverId2 = UUID.randomUUID();

        scheduleDto1 = ScheduleResponseDto.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .specificDate(null)
                .oneTime(false)
                .build();

        scheduleDto2 = ScheduleResponseDto.builder()
                .id(UUID.randomUUID())
                .caregiverId(caregiverId)
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .specificDate(LocalDate.now().plusDays(7))
                .oneTime(true)
                .build();

        scheduleList = Arrays.asList(scheduleDto1, scheduleDto2);
    }

    @Test
    void testGetCaregiverAvailableSchedules_Success() {
        String caregiverIdString = caregiverId.toString();
        when(scheduleService.getAvailableSchedulesByCaregiver(caregiverId)).thenReturn(scheduleList);

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getCaregiverAvailableSchedules(caregiverIdString);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Available schedules retrieved successfully", response.getBody().getMessage());
        assertEquals(scheduleList, response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService).getAvailableSchedulesByCaregiver(caregiverId);
    }

    @Test
    void testGetCaregiverAvailableSchedules_EmptyList() {
        String caregiverIdString = caregiverId.toString();
        List<ScheduleResponseDto> emptyList = Collections.emptyList();
        when(scheduleService.getAvailableSchedulesByCaregiver(caregiverId)).thenReturn(emptyList);

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getCaregiverAvailableSchedules(caregiverIdString);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Available schedules retrieved successfully", response.getBody().getMessage());
        assertEquals(emptyList, response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService).getAvailableSchedulesByCaregiver(caregiverId);
    }

    @Test
    void testGetCaregiverAvailableSchedules_InvalidUUID() {
        String invalidCaregiverIdString = "invalid-uuid";

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getCaregiverAvailableSchedules(invalidCaregiverIdString);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid caregiver ID format", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService, never()).getAvailableSchedulesByCaregiver(any());
    }

    @Test
    void testGetCaregiverAvailableSchedules_ServiceException() {
        String caregiverIdString = caregiverId.toString();
        when(scheduleService.getAvailableSchedulesByCaregiver(caregiverId))
                .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getCaregiverAvailableSchedules(caregiverIdString);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error retrieving schedules", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService).getAvailableSchedulesByCaregiver(caregiverId);
    }

    @Test
    void testGetAvailableSchedulesForMultipleCaregivers_WithCaregiverIds() {
        List<String> caregiverIdStrings = Arrays.asList(
                caregiverId.toString(),
                caregiverId2.toString()
        );
        List<UUID> caregiverUuids = Arrays.asList(caregiverId, caregiverId2);

        when(scheduleService.getAvailableSchedulesForCaregivers(caregiverUuids)).thenReturn(scheduleList);

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getAvailableSchedulesForMultipleCaregivers(caregiverIdStrings);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Available schedules retrieved successfully", response.getBody().getMessage());
        assertEquals(scheduleList, response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService).getAvailableSchedulesForCaregivers(caregiverUuids);
    }

    @Test
    void testGetAvailableSchedulesForMultipleCaregivers_NullCaregiverIds() {
        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getAvailableSchedulesForMultipleCaregivers(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Available schedules retrieved successfully", response.getBody().getMessage());
        assertEquals(Collections.emptyList(), response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService, never()).getAvailableSchedulesForCaregivers(any());
    }

    @Test
    void testGetAvailableSchedulesForMultipleCaregivers_EmptyCaregiverIds() {
        List<String> emptyCaregiverIdStrings = Collections.emptyList();

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getAvailableSchedulesForMultipleCaregivers(emptyCaregiverIdStrings);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Available schedules retrieved successfully", response.getBody().getMessage());
        assertEquals(Collections.emptyList(), response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService, never()).getAvailableSchedulesForCaregivers(any());
    }

    @Test
    void testGetAvailableSchedulesForMultipleCaregivers_InvalidUUID() {
        List<String> invalidCaregiverIdStrings = Arrays.asList("invalid-uuid");

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getAvailableSchedulesForMultipleCaregivers(invalidCaregiverIdStrings);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error retrieving available schedules", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService, never()).getAvailableSchedulesForCaregivers(any());
    }

    @Test
    void testGetAvailableSchedulesForMultipleCaregivers_ServiceException() {
        List<String> caregiverIdStrings = Arrays.asList(caregiverId.toString());
        List<UUID> caregiverUuids = Arrays.asList(caregiverId);

        when(scheduleService.getAvailableSchedulesForCaregivers(caregiverUuids))
                .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                dataController.getAvailableSchedulesForMultipleCaregivers(caregiverIdStrings);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error retrieving available schedules", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(availableSchedulesRequestCounter).increment();
        verify(scheduleService).getAvailableSchedulesForCaregivers(caregiverUuids);
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<ApiResponseDto<String>> response = dataController.healthCheck();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Doctor service is running", response.getBody().getMessage());
        assertEquals("OK", response.getBody().getData());

        verify(availableSchedulesRequestCounter, never()).increment();
        verify(scheduleService, never()).getAvailableSchedulesByCaregiver(any());
        verify(scheduleService, never()).getAvailableSchedulesForCaregivers(any());
    }
}