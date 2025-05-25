package id.ac.ui.cs.advprog.bekonsultasi.dto;

import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenVerificationResponseDtoTest {

    @Nested
    class ConstructorTests {
        @Test
        void testBuilderAndGetters() {
            TokenVerificationResponseDto dto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId("user-123")
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();
            
            assertTrue(dto.isValid());
            assertEquals("user-123", dto.getUserId());
            assertEquals("test@example.com", dto.getEmail());
            assertEquals(Role.CAREGIVER, dto.getRole());
            assertEquals(3600L, dto.getExpiresIn());
        }
        
        @Test
        void testNoArgsConstructor() {
            TokenVerificationResponseDto dto = new TokenVerificationResponseDto();
            
            assertFalse(dto.isValid()); 
            assertNull(dto.getUserId());
            assertNull(dto.getEmail());
            assertNull(dto.getRole());
            assertNull(dto.getExpiresIn());
        }
    }
    
    @Nested
    class PropertyAccessTests {
        @Test
        void testSetters() {
            TokenVerificationResponseDto dto = new TokenVerificationResponseDto();
            dto.setValid(true);
            dto.setUserId("user-123");
            dto.setEmail("test@example.com");
            dto.setRole(Role.CAREGIVER);
            dto.setExpiresIn(3600L);
            
            assertTrue(dto.isValid());
            assertEquals("user-123", dto.getUserId());
            assertEquals("test@example.com", dto.getEmail());
            assertEquals(Role.CAREGIVER, dto.getRole());
            assertEquals(3600L, dto.getExpiresIn());
        }
    }
    
    @Nested
    class ObjectMethodsTests {
        @Test
        void testEqualsAndHashCode() {
            TokenVerificationResponseDto dto1 = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId("user-123")
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();
            
            TokenVerificationResponseDto dto2 = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId("user-123")
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();
            
            TokenVerificationResponseDto dto3 = TokenVerificationResponseDto.builder()
                    .valid(false)
                    .userId("user-456")
                    .email("other@example.com")
                    .role(Role.PACILIAN)
                    .expiresIn(7200L)
                    .build();
            
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            
            assertNotEquals(dto1, dto3);
            assertNotEquals(dto1.hashCode(), dto3.hashCode());
        }
        
        @Test
        void testToString() {
            TokenVerificationResponseDto dto = TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId("user-123")
                    .email("test@example.com")
                    .role(Role.CAREGIVER)
                    .expiresIn(3600L)
                    .build();
            
            String toString = dto.toString();
            
            assertTrue(toString.contains("true"));
            assertTrue(toString.contains("user-123"));
            assertTrue(toString.contains("test@example.com"));
            assertTrue(toString.contains("CAREGIVER"));
            assertTrue(toString.contains("3600"));
        }
    }
}