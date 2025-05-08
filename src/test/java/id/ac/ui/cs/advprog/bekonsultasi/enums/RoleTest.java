package id.ac.ui.cs.advprog.bekonsultasi.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    public void testRoleEnum_AvailableValues() {
        assertEquals(2, Role.values().length);
        assertEquals(Role.PACILIAN, Role.valueOf("PACILIAN"));
        assertEquals(Role.CAREGIVER, Role.valueOf("CAREGIVER"));
    }
    
    @Test
    public void testRoleEnum_Ordinals() {
        assertEquals(0, Role.PACILIAN.ordinal());
        assertEquals(1, Role.CAREGIVER.ordinal());
    }
    
    @Test
    public void testRoleEnum_GetValue() {
        assertEquals("PACILIAN", Role.PACILIAN.getValue());
        assertEquals("CAREGIVER", Role.CAREGIVER.getValue());
    }
    
    @Test
    public void testRoleEnum_Equality() {
        Role role1 = Role.PACILIAN;
        Role role2 = Role.PACILIAN;
        Role role3 = Role.CAREGIVER;
        
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
    }
    
    @Test
    public void testRoleEnum_ValueOf_InvalidName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Role.valueOf("INVALID_ROLE");
        });
        
        assertTrue(exception.getMessage().contains("No enum constant"));
    }
    
    @Test
    public void testRoleEnum_Contains_ValidValues() {
        assertTrue(Role.contains("PACILIAN"));
        assertTrue(Role.contains("CAREGIVER"));
    }
    
    @Test
    public void testRoleEnum_Contains_InvalidValues() {
        assertFalse(Role.contains("ADMIN"));
        assertFalse(Role.contains("USER"));
        assertFalse(Role.contains(""));
        assertFalse(Role.contains(null));
    }
    
    @Test
    public void testRoleEnum_Contains_CaseSensitivity() {
        assertFalse(Role.contains("pacilian"));
        assertFalse(Role.contains("Pacilian"));
        assertFalse(Role.contains("caregiver"));
        assertFalse(Role.contains("Caregiver"));
    }
    
    @Test
    public void testRoleEnum_ToString() {
        assertEquals("PACILIAN", Role.PACILIAN.toString());
        assertEquals("CAREGIVER", Role.CAREGIVER.toString());
    }
}