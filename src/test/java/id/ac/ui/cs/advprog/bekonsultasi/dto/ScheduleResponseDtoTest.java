package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleResponseDtoTest {
    @Test
    void testBuilder() {
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        ScheduleResponseDto dto = ScheduleResponseDto.builder()
                .id(id)
                .caregiverId(caregiverId)
                .day(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status("AVAILABLE")
                .build();

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(DayOfWeek.MONDAY, dto.getDay());
        assertEquals(LocalTime.of(9, 0), dto.getStartTime());
        assertEquals(LocalTime.of(10, 0), dto.getEndTime());
        assertEquals("AVAILABLE", dto.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        ScheduleResponseDto dto = new ScheduleResponseDto(
                id,
                caregiverId,
                DayOfWeek.TUESDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "REQUESTED"
        );

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(DayOfWeek.TUESDAY, dto.getDay());
        assertEquals(LocalTime.of(10, 0), dto.getStartTime());
        assertEquals(LocalTime.of(11, 0), dto.getEndTime());
        assertEquals("REQUESTED", dto.getStatus());
    }

    @Test
    void testNoArgsConstructor() {
        ScheduleResponseDto dto = new ScheduleResponseDto();
        assertNull(dto.getId());
        assertNull(dto.getCaregiverId());
        assertNull(dto.getDay());
        assertNull(dto.getStartTime());
        assertNull(dto.getEndTime());
        assertNull(dto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();

        ScheduleResponseDto dto = new ScheduleResponseDto();
        dto.setId(id);
        dto.setCaregiverId(caregiverId);
        dto.setDay(DayOfWeek.WEDNESDAY);
        dto.setStartTime(LocalTime.of(13, 0));
        dto.setEndTime(LocalTime.of(14, 0));
        dto.setStatus("APPROVED");

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(DayOfWeek.WEDNESDAY, dto.getDay());
        assertEquals(LocalTime.of(13, 0), dto.getStartTime());
        assertEquals(LocalTime.of(14, 0), dto.getEndTime());
        assertEquals("APPROVED", dto.getStatus());
    }
}