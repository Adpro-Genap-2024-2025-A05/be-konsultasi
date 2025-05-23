package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateKonsultasiDtoTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testBuilder() {
        UUID scheduleId = UUID.randomUUID();
        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(1);
        String notes = "Test notes";

        CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .scheduleDateTime(scheduleDateTime)
                .notes(notes)
                .build();

        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals(scheduleDateTime, dto.getScheduleDateTime());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testNoArgsConstructor() {
        CreateKonsultasiDto dto = new CreateKonsultasiDto();

        assertNull(dto.getScheduleId());
        assertNull(dto.getScheduleDateTime());
        assertNull(dto.getNotes());
    }

    @Test
    void testAllArgsConstructor() {
        UUID scheduleId = UUID.randomUUID();
        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(1);
        String notes = "Test notes";

        CreateKonsultasiDto dto = new CreateKonsultasiDto(scheduleId, scheduleDateTime, notes);

        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals(scheduleDateTime, dto.getScheduleDateTime());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testGettersAndSetters() {
        CreateKonsultasiDto dto = new CreateKonsultasiDto();

        UUID scheduleId = UUID.randomUUID();
        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(1);
        String notes = "Test notes";

        dto.setScheduleId(scheduleId);
        dto.setScheduleDateTime(scheduleDateTime);
        dto.setNotes(notes);

        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals(scheduleDateTime, dto.getScheduleDateTime());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testValidation() {
        CreateKonsultasiDto dto = new CreateKonsultasiDto();

        var violations = validator.validate(dto);
        assertEquals(2, violations.size());

        dto.setScheduleId(UUID.randomUUID());
        violations = validator.validate(dto);
        assertEquals(1, violations.size());

        dto.setScheduleDateTime(LocalDateTime.now().plusDays(1));
        violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }
}