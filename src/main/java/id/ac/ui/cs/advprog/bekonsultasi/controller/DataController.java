package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.ScheduleResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DataController {

    private final ScheduleService scheduleService;

    @GetMapping(path = "/caregiver/{caregiverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> getCaregiverSchedules(
            @PathVariable String caregiverId) {

        try {
            UUID caregiverUuid = UUID.fromString(caregiverId);
            List<ScheduleResponseDto> schedules = scheduleService.getCaregiverSchedules(caregiverUuid);

            return ResponseEntity.ok(
                    ApiResponseDto.success(200,
                            "Caregiver schedules retrieved successfully",
                            schedules)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error(400, "Invalid caregiver ID format"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error(500, "Error retrieving schedules"));
        }
    }

    @GetMapping(path = "/schedules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<ScheduleResponseDto>>> getSchedulesForMultipleCaregivers(
            @RequestParam(required = false) List<String> caregiverIds) {

        try {
            List<ScheduleResponseDto> schedules;

            if (caregiverIds != null && !caregiverIds.isEmpty()) {
                List<UUID> uuidList = caregiverIds.stream()
                        .map(UUID::fromString)
                        .toList();
                schedules = scheduleService.getSchedulesForCaregivers(uuidList);
            } else {
                schedules = scheduleService.getAllSchedules();
            }

            return ResponseEntity.ok(
                    ApiResponseDto.success(200,
                            "Schedules retrieved successfully",
                            schedules)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error(500, "Error retrieving schedules"));
        }
    }

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponseDto.success(200,
                        "Konsultasi service is running",
                        "OK")
        );
    }
}