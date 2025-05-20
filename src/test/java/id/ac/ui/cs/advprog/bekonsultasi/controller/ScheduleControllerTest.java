package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleConflictException;
import id.ac.ui.cs.advprog.bekonsultasi.exception.ScheduleException;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import id.ac.ui.cs.advprog.bekonsultasi.service.TokenVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
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

    @InjectMocks
    private ScheduleController scheduleController;

    private UUID caregiverId;
    private UUID scheduleId;
    private String token;
    private MockHttpServletRequest request;
    private TokenVerificationResponseDto tokenResponse;
    private CreateScheduleDto createScheduleDto;
    private ScheduleResponseDto scheduleResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        caregiverId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        token = "test-token";

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        tokenResponse = TokenVerificationResponseDto.builder()
                .valid(true)
                .userId(caregiverId.toString())
                .role(Role.CAREGIVER)
                .expiresIn(3600L)
                .build();

        createScheduleDto = CreateScheduleDto.builder()
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        scheduleResponse = ScheduleResponseDto.builder()
                .id(scheduleId)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();
    }

    @Nested
    class CreateScheduleTests {
        @Test
        void createCaregiverSchedule_shouldReturnCreatedSchedule() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            when(scheduleService.createSchedule(eq(createScheduleDto), eq(caregiverId))).thenReturn(scheduleResponse);

            ResponseEntity<ApiResponseDto<ScheduleResponseDto>> response =
                    scheduleController.createCaregiverSchedule(createScheduleDto, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(201, response.getBody().getStatus());
            assertEquals("Created successfully", response.getBody().getMessage());
            assertEquals(scheduleResponse, response.getBody().getData());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService).createSchedule(eq(createScheduleDto), eq(caregiverId));
        }

        @Test
        void createCaregiverSchedule_shouldThrowExceptionForNonCaregivers() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.createCaregiverSchedule(createScheduleDto, request);
            });

            assertEquals("Only caregivers can create schedules", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService, never()).createSchedule(any(), any());
        }
    }

    @Nested
    class UpdateScheduleTests {
        @Test
        void updateCaregiverSchedule_shouldReturnUpdatedSchedule() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            ScheduleResponseDto updatedResponse = ScheduleResponseDto.builder()
                    .id(scheduleId)
                    .caregiverId(caregiverId)
                    .day(DayOfWeek.TUESDAY)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .status("AVAILABLE")
                    .build();

            CreateScheduleDto updateDto = CreateScheduleDto.builder()
                    .day(DayOfWeek.TUESDAY)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .build();

            when(scheduleService.updateSchedule(eq(scheduleId), eq(updateDto), eq(caregiverId)))
                    .thenReturn(updatedResponse);

            ResponseEntity<ApiResponseDto<ScheduleResponseDto>> response =
                    scheduleController.updateCaregiverSchedule(scheduleId, updateDto, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Schedule updated successfully", response.getBody().getMessage());
            assertEquals(updatedResponse, response.getBody().getData());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService).updateSchedule(eq(scheduleId), eq(updateDto), eq(caregiverId));
        }

        @Test
        void updateCaregiverSchedule_shouldThrowExceptionForNonCaregivers() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.updateCaregiverSchedule(scheduleId, createScheduleDto, request);
            });

            assertEquals("Only caregivers can update schedules", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService, never()).updateSchedule(any(), any(), any());
        }
    }

    @Nested
    class DeleteScheduleTests {
        @Test
        void deleteCaregiverSchedule_shouldReturnSuccess() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);
            doNothing().when(scheduleService).deleteSchedule(scheduleId, caregiverId);

            ResponseEntity<ApiResponseDto<Object>> response =
                    scheduleController.deleteCaregiverSchedule(scheduleId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Schedule deleted successfully", response.getBody().getMessage());
            assertNull(response.getBody().getData());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService).deleteSchedule(scheduleId, caregiverId);
        }

        @Test
        void deleteCaregiverSchedule_shouldThrowExceptionForNonCaregivers() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.deleteCaregiverSchedule(scheduleId, request);
            });

            assertEquals("Only caregivers can delete schedules", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService, never()).deleteSchedule(any(), any());
        }
    }

    @Nested
    class GetScheduleTests {
        @Test
        void getCurrentCaregiverSchedules_shouldReturnCaregiverSchedules() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            ScheduleResponseDto schedule2 = ScheduleResponseDto.builder()
                    .id(UUID.randomUUID())
                    .caregiverId(caregiverId)
                    .day(DayOfWeek.TUESDAY)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .status("AVAILABLE")
                    .build();

            List<ScheduleResponseDto> expectedSchedules = Arrays.asList(scheduleResponse, schedule2);

            when(scheduleService.getCaregiverSchedules(caregiverId)).thenReturn(expectedSchedules);

            ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                    scheduleController.getCurrentCaregiverSchedules(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Retrieved caregiver schedules", response.getBody().getMessage());
            assertEquals(expectedSchedules, response.getBody().getData());
            assertEquals(2, response.getBody().getData().size());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService).getCaregiverSchedules(caregiverId);
        }

        @Test
        void getCurrentCaregiverSchedules_shouldThrowExceptionForNonCaregivers() {
            tokenResponse.setRole(Role.PACILIAN);
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.getCurrentCaregiverSchedules(request);
            });

            assertEquals("Only caregivers can view their schedules", exception.getMessage());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService, never()).getCaregiverSchedules(any());
        }

        @Test
        void getCaregiverSchedulesByIdParam_shouldReturnSchedules() {
            when(tokenVerificationService.verifyToken(token)).thenReturn(tokenResponse);

            UUID targetCaregiverId = UUID.randomUUID();
            List<ScheduleResponseDto> expectedSchedules = Arrays.asList(scheduleResponse);

            when(scheduleService.getCaregiverSchedules(targetCaregiverId)).thenReturn(expectedSchedules);

            ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> response =
                    scheduleController.getCaregiverSchedulesByIdParam(targetCaregiverId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getStatus());
            assertEquals("Retrieved caregiver schedules", response.getBody().getMessage());
            assertEquals(expectedSchedules, response.getBody().getData());
            verify(tokenVerificationService).verifyToken(token);
            verify(scheduleService).getCaregiverSchedules(targetCaregiverId);
        }
    }

    @Nested
    class ExceptionHandlingTests {
        @Test
        void handleIllegalArgumentException_shouldReturnBadRequest() {
            String errorMessage = "Test error message";
            IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

            ResponseEntity<ApiResponseDto<Object>> response =
                    scheduleController.handleIllegalArgumentException(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }

        @Test
        void handleScheduleConflictException_shouldReturnConflict() {
            String errorMessage = "Test conflict message";
            ScheduleConflictException ex = new ScheduleConflictException(errorMessage);

            ResponseEntity<ApiResponseDto<Object>> response =
                    scheduleController.handleScheduleConflictException(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(409, response.getBody().getStatus());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }

        @Test
        void handleAuthenticationException_shouldReturnUnauthorized() {
            String errorMessage = "Test auth message";
            AuthenticationException ex = new AuthenticationException(errorMessage);

            ResponseEntity<ApiResponseDto<Object>> response =
                    scheduleController.handleAuthenticationException(ex);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(401, response.getBody().getStatus());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }

        @Test
        void handleScheduleException_shouldReturnBadRequest() {
            String errorMessage = "Test schedule error";
            ScheduleException ex = new ScheduleException(errorMessage);

            ResponseEntity<ApiResponseDto<Object>> response =
                    scheduleController.handleScheduleException(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertNull(response.getBody().getData());
        }
    }

    @Nested
    class AuthenticationTests {
        @Test
        void extractToken_shouldThrowExceptionForMissingAuthHeader() {
            MockHttpServletRequest invalidRequest = new MockHttpServletRequest();

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.getCurrentCaregiverSchedules(invalidRequest);
            });

            assertEquals("Authorization header is missing or invalid", exception.getMessage());
            verify(tokenVerificationService, never()).verifyToken(any());
        }

        @Test
        void extractToken_shouldThrowExceptionForInvalidAuthHeader() {
            MockHttpServletRequest invalidRequest = new MockHttpServletRequest();
            invalidRequest.addHeader("Authorization", "InvalidHeader");

            Exception exception = assertThrows(AuthenticationException.class, () -> {
                scheduleController.getCurrentCaregiverSchedules(invalidRequest);
            });

            assertEquals("Authorization header is missing or invalid", exception.getMessage());
            verify(tokenVerificationService, never()).verifyToken(any());
        }
    }
}