package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateKonsultasiDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreateKonsultasiDto() {
        UUID scheduleId = UUID.randomUUID();
        CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Test notes")
                .build();

        Set<ConstraintViolation<CreateKonsultasiDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        
        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals("Test notes", dto.getNotes());
    }

    @Test
    void testInvalidCreateKonsultasiDtoNullScheduleId() {
        CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                .scheduleId(null)
                .notes("Test notes")
                .build();

        Set<ConstraintViolation<CreateKonsultasiDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        
        ConstraintViolation<CreateKonsultasiDto> violation = violations.iterator().next();
        assertEquals("Schedule ID is required", violation.getMessage());
    }
    
    @Test
    void testBuilderAndNoArgsConstructor() {
        UUID scheduleId = UUID.randomUUID();
        
        CreateKonsultasiDto builderDto = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Builder notes")
                .build();
        
        CreateKonsultasiDto noArgsDto = new CreateKonsultasiDto();
        noArgsDto.setScheduleId(scheduleId);
        noArgsDto.setNotes("NoArgs notes");
        
        assertEquals(scheduleId, builderDto.getScheduleId());
        assertEquals("Builder notes", builderDto.getNotes());
        
        assertEquals(scheduleId, noArgsDto.getScheduleId());
        assertEquals("NoArgs notes", noArgsDto.getNotes());
    }
    
    @Test
    void testAllArgsConstructor() {
        UUID scheduleId = UUID.randomUUID();
        
        CreateKonsultasiDto dto = new CreateKonsultasiDto(scheduleId, "All args notes");
        
        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals("All args notes", dto.getNotes());
    }
    
    @Test
    void testEqualsAndHashCode() {
        UUID scheduleId = UUID.randomUUID();
        
        CreateKonsultasiDto dto1 = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Notes")
                .build();
        
        CreateKonsultasiDto dto2 = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Notes")
                .build();
        
        CreateKonsultasiDto dto3 = CreateKonsultasiDto.builder()
                .scheduleId(UUID.randomUUID())
                .notes("Different notes")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testToString() {
        UUID scheduleId = UUID.randomUUID();
        
        CreateKonsultasiDto dto = CreateKonsultasiDto.builder()
                .scheduleId(scheduleId)
                .notes("Test notes")
                .build();
        
        String toString = dto.toString();
        
        assertTrue(toString.contains(scheduleId.toString()));
        assertTrue(toString.contains("Test notes"));
    }
}