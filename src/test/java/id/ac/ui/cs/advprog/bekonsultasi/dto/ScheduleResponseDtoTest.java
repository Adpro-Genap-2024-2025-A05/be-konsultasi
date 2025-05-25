package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleResponseDtoTest {

    @Test
    void testBuilderAndGetters() {
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        boolean oneTime = true;

        ScheduleResponseDto dto = ScheduleResponseDto.builder()
                .id(id)
                .caregiverId(caregiverId)
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .specificDate(specificDate)
                .oneTime(oneTime)
                .build();

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(day, dto.getDay());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
        assertEquals(specificDate, dto.getSpecificDate());
        assertTrue(dto.isOneTime());
    }

    @Test
    void testNoArgsConstructor() {
        ScheduleResponseDto dto = new ScheduleResponseDto();

        assertNull(dto.getId());
        assertNull(dto.getCaregiverId());
        assertNull(dto.getDay());
        assertNull(dto.getStartTime());
        assertNull(dto.getEndTime());
        assertNull(dto.getSpecificDate());
        assertFalse(dto.isOneTime());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        boolean oneTime = true;

        ScheduleResponseDto dto = new ScheduleResponseDto(
                id, caregiverId, day, startTime, endTime, specificDate, oneTime);

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(day, dto.getDay());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
        assertEquals(specificDate, dto.getSpecificDate());
        assertTrue(dto.isOneTime());
    }

    @Test
    void testSetters() {
        ScheduleResponseDto dto = new ScheduleResponseDto();

        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LocalDate specificDate = LocalDate.of(2025, 6, 2);

        dto.setId(id);
        dto.setCaregiverId(caregiverId);
        dto.setDay(day);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setSpecificDate(specificDate);
        dto.setOneTime(true);

        assertEquals(id, dto.getId());
        assertEquals(caregiverId, dto.getCaregiverId());
        assertEquals(day, dto.getDay());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
        assertEquals(specificDate, dto.getSpecificDate());
        assertTrue(dto.isOneTime());
    }
}