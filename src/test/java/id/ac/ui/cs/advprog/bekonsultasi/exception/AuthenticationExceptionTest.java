package id.ac.ui.cs.advprog.bekonsultasi.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void testConstructorAndGetMessage() {
        String errorMessage = "Invalid token";
        AuthenticationException exception = new AuthenticationException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }
    
    @Test
    void testExceptionHierarchy() {
        AuthenticationException exception = new AuthenticationException("Test exception");
        
        assertTrue(exception instanceof RuntimeException);
    }
    
    @Test
    void testExceptionPropagation() {
        try {
            throw new AuthenticationException("Authentication failed");
        } catch (RuntimeException e) {
            assertTrue(e instanceof AuthenticationException);
            assertEquals("Authentication failed", e.getMessage());
        }
    }
}