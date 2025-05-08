package id.ac.ui.cs.advprog.bekonsultasi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RescheduleKonsultasiDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    class ValidationTests {
        @Test
        void testValidRescheduleKonsultasiDto() {
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);
            RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(newDateTime)
                    .notes("Reschedule notes")
                    .build();

            Set<ConstraintViolation<RescheduleKonsultasiDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
            
            assertEquals(newDateTime, dto.getNewScheduleDateTime());
            assertEquals("Reschedule notes", dto.getNotes());
        }

        @Test
        void testInvalidRescheduleKonsultasiDtoNullDateTime() {
            RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(null)
                    .notes("Reschedule notes")
                    .build();

            Set<ConstraintViolation<RescheduleKonsultasiDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            
            ConstraintViolation<RescheduleKonsultasiDto> violation = violations.iterator().next();
            assertEquals("New schedule date and time is required", violation.getMessage());
        }
    }
    
    @Nested
    class ConstructorTests {
        @Test
        void testBuilderAndNoArgsConstructor() {
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);
            
            RescheduleKonsultasiDto builderDto = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(newDateTime)
                    .notes("Builder notes")
                    .build();
            
            RescheduleKonsultasiDto noArgsDto = new RescheduleKonsultasiDto();
            noArgsDto.setNewScheduleDateTime(newDateTime);
            noArgsDto.setNotes("NoArgs notes");
            
            assertEquals(newDateTime, builderDto.getNewScheduleDateTime());
            assertEquals("Builder notes", builderDto.getNotes());
            
            assertEquals(newDateTime, noArgsDto.getNewScheduleDateTime());
            assertEquals("NoArgs notes", noArgsDto.getNotes());
        }
        
        @Test
        void testAllArgsConstructor() {
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);
            
            RescheduleKonsultasiDto dto = new RescheduleKonsultasiDto(newDateTime, "All args notes");
            
            assertEquals(newDateTime, dto.getNewScheduleDateTime());
            assertEquals("All args notes", dto.getNotes());
        }
    }
    
    @Nested
    class ObjectMethodsTests {
        @Test
        void testEqualsAndHashCode() {
            LocalDateTime dateTime1 = LocalDateTime.now().plusDays(7);
            LocalDateTime dateTime2 = LocalDateTime.now().plusDays(14);
            
            RescheduleKonsultasiDto dto1 = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(dateTime1)
                    .notes("Notes")
                    .build();
            
            RescheduleKonsultasiDto dto2 = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(dateTime1)
                    .notes("Notes")
                    .build();
            
            RescheduleKonsultasiDto dto3 = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(dateTime2)
                    .notes("Different notes")
                    .build();
            
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            
            assertNotEquals(dto1, dto3);
            assertNotEquals(dto1.hashCode(), dto3.hashCode());
        }
        
        @Test
        void testToString() {
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(7);
            
            RescheduleKonsultasiDto dto = RescheduleKonsultasiDto.builder()
                    .newScheduleDateTime(newDateTime)
                    .notes("Test notes")
                    .build();
            
            String toString = dto.toString();
            
            assertTrue(toString.contains(newDateTime.toString()));
            assertTrue(toString.contains("Test notes"));
        }
    }
}