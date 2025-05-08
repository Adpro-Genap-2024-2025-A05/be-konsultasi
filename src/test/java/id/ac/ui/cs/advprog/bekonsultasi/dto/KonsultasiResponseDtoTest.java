package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KonsultasiResponseDtoTest {

    @Nested
    class ConstructorTests {
        @Test
        void testBuilderAndGetters() {
            UUID id = UUID.randomUUID();
            UUID scheduleId = UUID.randomUUID();
            UUID caregiverId = UUID.randomUUID();
            UUID pacilianId = UUID.randomUUID();
            LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);
            LocalDateTime lastUpdated = LocalDateTime.now();
            
            KonsultasiResponseDto dto = KonsultasiResponseDto.builder()
                    .id(id)
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes("Test notes")
                    .status("REQUESTED")
                    .lastUpdated(lastUpdated)
                    .build();
            
            assertEquals(id, dto.getId());
            assertEquals(scheduleId, dto.getScheduleId());
            assertEquals(caregiverId, dto.getCaregiverId());
            assertEquals(pacilianId, dto.getPacilianId());
            assertEquals(scheduleDateTime, dto.getScheduleDateTime());
            assertEquals("Test notes", dto.getNotes());
            assertEquals("REQUESTED", dto.getStatus());
            assertEquals(lastUpdated, dto.getLastUpdated());
        }
        
        @Test
        void testNoArgsConstructor() {
            KonsultasiResponseDto dto = new KonsultasiResponseDto();
            
            assertNull(dto.getId());
            assertNull(dto.getScheduleId());
            assertNull(dto.getCaregiverId());
            assertNull(dto.getPacilianId());
            assertNull(dto.getScheduleDateTime());
            assertNull(dto.getNotes());
            assertNull(dto.getStatus());
            assertNull(dto.getLastUpdated());
        }
        
        @Test
        void testAllArgsConstructor() {
            UUID id = UUID.randomUUID();
            UUID scheduleId = UUID.randomUUID();
            UUID caregiverId = UUID.randomUUID();
            UUID pacilianId = UUID.randomUUID();
            LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);
            LocalDateTime lastUpdated = LocalDateTime.now();
            
            KonsultasiResponseDto dto = new KonsultasiResponseDto(
                    id, scheduleId, caregiverId, pacilianId,
                    scheduleDateTime, "Test notes", "CONFIRMED", lastUpdated
            );
            
            assertEquals(id, dto.getId());
            assertEquals(scheduleId, dto.getScheduleId());
            assertEquals(caregiverId, dto.getCaregiverId());
            assertEquals(pacilianId, dto.getPacilianId());
            assertEquals(scheduleDateTime, dto.getScheduleDateTime());
            assertEquals("Test notes", dto.getNotes());
            assertEquals("CONFIRMED", dto.getStatus());
            assertEquals(lastUpdated, dto.getLastUpdated());
        }
    }
    
    @Nested
    class PropertyAccessTests {
        @Test
        void testSetters() {
            UUID id = UUID.randomUUID();
            UUID scheduleId = UUID.randomUUID();
            UUID caregiverId = UUID.randomUUID();
            UUID pacilianId = UUID.randomUUID();
            LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);
            LocalDateTime lastUpdated = LocalDateTime.now();
            
            KonsultasiResponseDto dto = new KonsultasiResponseDto();
            dto.setId(id);
            dto.setScheduleId(scheduleId);
            dto.setCaregiverId(caregiverId);
            dto.setPacilianId(pacilianId);
            dto.setScheduleDateTime(scheduleDateTime);
            dto.setNotes("Setting notes");
            dto.setStatus("DONE");
            dto.setLastUpdated(lastUpdated);
            
            assertEquals(id, dto.getId());
            assertEquals(scheduleId, dto.getScheduleId());
            assertEquals(caregiverId, dto.getCaregiverId());
            assertEquals(pacilianId, dto.getPacilianId());
            assertEquals(scheduleDateTime, dto.getScheduleDateTime());
            assertEquals("Setting notes", dto.getNotes());
            assertEquals("DONE", dto.getStatus());
            assertEquals(lastUpdated, dto.getLastUpdated());
        }
    }
    
    @Nested
    class ObjectMethodsTests {
        @Test
        void testEqualsAndHashCode() {
            UUID id = UUID.randomUUID();
            UUID scheduleId = UUID.randomUUID();
            UUID caregiverId = UUID.randomUUID();
            UUID pacilianId = UUID.randomUUID();
            LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);
            LocalDateTime lastUpdated = LocalDateTime.now();
            
            KonsultasiResponseDto dto1 = KonsultasiResponseDto.builder()
                    .id(id)
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes("Test notes")
                    .status("REQUESTED")
                    .lastUpdated(lastUpdated)
                    .build();
            
            KonsultasiResponseDto dto2 = KonsultasiResponseDto.builder()
                    .id(id)
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes("Test notes")
                    .status("REQUESTED")
                    .lastUpdated(lastUpdated)
                    .build();
            
            KonsultasiResponseDto dto3 = KonsultasiResponseDto.builder()
                    .id(UUID.randomUUID())
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes("Different notes")
                    .status("CONFIRMED")
                    .lastUpdated(lastUpdated)
                    .build();
            
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            
            assertNotEquals(dto1, dto3);
            assertNotEquals(dto1.hashCode(), dto3.hashCode());
        }
        
        @Test
        void testToString() {
            UUID id = UUID.randomUUID();
            UUID scheduleId = UUID.randomUUID();
            UUID caregiverId = UUID.randomUUID();
            UUID pacilianId = UUID.randomUUID();
            LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(7);
            
            KonsultasiResponseDto dto = KonsultasiResponseDto.builder()
                    .id(id)
                    .scheduleId(scheduleId)
                    .caregiverId(caregiverId)
                    .pacilianId(pacilianId)
                    .scheduleDateTime(scheduleDateTime)
                    .notes("Test notes")
                    .status("REQUESTED")
                    .build();
            
            String toString = dto.toString();
            
            assertTrue(toString.contains(id.toString()));
            assertTrue(toString.contains(scheduleId.toString()));
            assertTrue(toString.contains(caregiverId.toString()));
            assertTrue(toString.contains(pacilianId.toString()));
            assertTrue(toString.contains("Test notes"));
            assertTrue(toString.contains("REQUESTED"));
        }
    }
}