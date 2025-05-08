package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KonsultasiHistoryDtoTest {

    @Test
    void testBuilderAndGetters() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        String modifiedByUserType = "CAREGIVER";
        
        KonsultasiHistoryDto dto = KonsultasiHistoryDto.builder()
                .id(id)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(timestamp)
                .modifiedByUserType(modifiedByUserType)
                .notes("Test notes")
                .build();
        
        assertEquals(id, dto.getId());
        assertEquals("REQUESTED", dto.getPreviousStatus());
        assertEquals("CONFIRMED", dto.getNewStatus());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(modifiedByUserType, dto.getModifiedByUserType());
        assertEquals("Test notes", dto.getNotes());
    }
    
    @Test
    void testNoArgsConstructor() {
        KonsultasiHistoryDto dto = new KonsultasiHistoryDto();
        
        assertNull(dto.getId());
        assertNull(dto.getPreviousStatus());
        assertNull(dto.getNewStatus());
        assertNull(dto.getTimestamp());
        assertNull(dto.getModifiedByUserType());
        assertNull(dto.getNotes());
    }
    
    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        String modifiedByUserType = "PACILIAN";
        
        KonsultasiHistoryDto dto = new KonsultasiHistoryDto(
                id, "CONFIRMED", "DONE", timestamp, modifiedByUserType, "Completed test"
        );
        
        assertEquals(id, dto.getId());
        assertEquals("CONFIRMED", dto.getPreviousStatus());
        assertEquals("DONE", dto.getNewStatus());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(modifiedByUserType, dto.getModifiedByUserType());
        assertEquals("Completed test", dto.getNotes());
    }
    
    @Test
    void testSetters() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        String modifiedByUserType = "CAREGIVER";
        
        KonsultasiHistoryDto dto = new KonsultasiHistoryDto();
        dto.setId(id);
        dto.setPreviousStatus("REQUESTED");
        dto.setNewStatus("CONFIRMED");
        dto.setTimestamp(timestamp);
        dto.setModifiedByUserType(modifiedByUserType);
        dto.setNotes("Setting test");
        
        assertEquals(id, dto.getId());
        assertEquals("REQUESTED", dto.getPreviousStatus());
        assertEquals("CONFIRMED", dto.getNewStatus());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(modifiedByUserType, dto.getModifiedByUserType());
        assertEquals("Setting test", dto.getNotes());
    }
    
    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        
        KonsultasiHistoryDto dto1 = KonsultasiHistoryDto.builder()
                .id(id)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(timestamp)
                .modifiedByUserType("CAREGIVER")
                .notes("Test notes")
                .build();
        
        KonsultasiHistoryDto dto2 = KonsultasiHistoryDto.builder()
                .id(id)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(timestamp)
                .modifiedByUserType("CAREGIVER")
                .notes("Test notes")
                .build();
        
        KonsultasiHistoryDto dto3 = KonsultasiHistoryDto.builder()
                .id(UUID.randomUUID())
                .previousStatus("CONFIRMED")
                .newStatus("DONE")
                .timestamp(LocalDateTime.now().plusDays(1))
                .modifiedByUserType("PACILIAN")
                .notes("Different notes")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testToString() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        
        KonsultasiHistoryDto dto = KonsultasiHistoryDto.builder()
                .id(id)
                .previousStatus("REQUESTED")
                .newStatus("CONFIRMED")
                .timestamp(timestamp)
                .modifiedByUserType("CAREGIVER")
                .notes("Test notes")
                .build();
        
        String toString = dto.toString();
        
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains("REQUESTED"));
        assertTrue(toString.contains("CONFIRMED"));
        assertTrue(toString.contains("CAREGIVER"));
        assertTrue(toString.contains("Test notes"));
    }
}