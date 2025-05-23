package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CreateScheduleDtoTest {
    private Validator validator;
    private CreateScheduleDto dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        dto = new CreateScheduleDto();
        dto.setDay(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));
    }

    @Test
    void testValidDto() {
        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullDay() {
        dto.setDay(null);
        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Day is required", violations.iterator().next().getMessage());
    }

    @Test
    void testNullStartTime() {
        dto.setStartTime(null);
        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Start time is required", violations.iterator().next().getMessage());
    }

    @Test
    void testNullEndTime() {
        dto.setEndTime(null);
        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("End time is required", violations.iterator().next().getMessage());
    }

    @Test
    void testBuilder() {
        CreateScheduleDto builtDto = CreateScheduleDto.builder()
                .day(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        assertEquals(DayOfWeek.TUESDAY, builtDto.getDay());
        assertEquals(LocalTime.of(10, 0), builtDto.getStartTime());
        assertEquals(LocalTime.of(11, 0), builtDto.getEndTime());
    }

    @Test
    void testAllArgsConstructor() {
        CreateScheduleDto allArgsDto = new CreateScheduleDto(
                DayOfWeek.WEDNESDAY,
                LocalTime.of(13, 0),
                LocalTime.of(14, 0)
        );

        assertEquals(DayOfWeek.WEDNESDAY, allArgsDto.getDay());
        assertEquals(LocalTime.of(13, 0), allArgsDto.getStartTime());
        assertEquals(LocalTime.of(14, 0), allArgsDto.getEndTime());
    }
}