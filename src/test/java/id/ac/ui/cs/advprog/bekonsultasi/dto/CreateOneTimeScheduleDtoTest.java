package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateOneTimeScheduleDtoTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testBuilderAndGetters() {
        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        CreateOneTimeScheduleDto dto = CreateOneTimeScheduleDto.builder()
                .specificDate(specificDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        assertEquals(specificDate, dto.getSpecificDate());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
    }

    @Test
    void testNoArgsConstructor() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();
        assertNull(dto.getSpecificDate());
        assertNull(dto.getStartTime());
        assertNull(dto.getEndTime());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto(specificDate, startTime, endTime);

        assertEquals(specificDate, dto.getSpecificDate());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
    }

    @Test
    void testSetters() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();

        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        dto.setSpecificDate(specificDate);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);

        assertEquals(specificDate, dto.getSpecificDate());
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
    }

    @Test
    void testValidation() {
        CreateOneTimeScheduleDto dto = new CreateOneTimeScheduleDto();

        var violations = validator.validate(dto);
        assertEquals(3, violations.size());

        dto.setSpecificDate(LocalDate.of(2025, 6, 2));
        violations = validator.validate(dto);
        assertEquals(2, violations.size());

        dto.setStartTime(LocalTime.of(10, 0));
        violations = validator.validate(dto);
        assertEquals(1, violations.size());

        dto.setEndTime(LocalTime.of(11, 0));
        violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }
}