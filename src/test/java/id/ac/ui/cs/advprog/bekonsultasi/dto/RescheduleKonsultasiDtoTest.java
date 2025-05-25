package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RescheduleKonsultasiDtoTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testBuilderAndGetters() {
        LocalDateTime newScheduleDateTime = LocalDateTime.now().plusDays(1);
        UUID newScheduleId = UUID.randomUUID();
        String notes = "Rescheduling notes";

        RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                .newScheduleDateTime(newScheduleDateTime)
                .newScheduleId(newScheduleId)
                .notes(notes)
                .build();

        assertEquals(newScheduleDateTime, dto.getNewScheduleDateTime());
        assertEquals(newScheduleId, dto.getNewScheduleId());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testNoArgsConstructor() {
        RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto();

        assertNull(dto.getNewScheduleDateTime());
        assertNull(dto.getNewScheduleId());
        assertNull(dto.getNotes());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime newScheduleDateTime = LocalDateTime.now().plusDays(1);
        UUID newScheduleId = UUID.randomUUID();
        String notes = "Rescheduling notes";

        RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto(newScheduleDateTime, newScheduleId, notes);

        assertEquals(newScheduleDateTime, dto.getNewScheduleDateTime());
        assertEquals(newScheduleId, dto.getNewScheduleId());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testSetters() {
        RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto();

        LocalDateTime newScheduleDateTime = LocalDateTime.now().plusDays(1);
        UUID newScheduleId = UUID.randomUUID();
        String notes = "Rescheduling notes";

        dto.setNewScheduleDateTime(newScheduleDateTime);
        dto.setNewScheduleId(newScheduleId);
        dto.setNotes(notes);

        assertEquals(newScheduleDateTime, dto.getNewScheduleDateTime());
        assertEquals(newScheduleId, dto.getNewScheduleId());
        assertEquals(notes, dto.getNotes());
    }

    @Test
    void testValidation() {
        RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto();

        var violations = validator.validate(dto);
        assertEquals(1, violations.size());

        dto.setNewScheduleDateTime(LocalDateTime.now().plusDays(1));
        violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_OptionalFields() {
        RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto();
        dto.setNewScheduleDateTime(LocalDateTime.now().plusDays(1));

        var violations = validator.validate(dto);
        assertEquals(0, violations.size());

        dto.setNewScheduleId(UUID.randomUUID());
        violations = validator.validate(dto);
        assertEquals(0, violations.size());

        dto.setNotes("Optional notes");
        violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }
}