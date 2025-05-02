package id.ac.ui.cs.advprog.bekonsultasi.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleConflictExceptionTest {
    @Test
    void testConstructorWithMessage() {
        String message = "Test conflict message";
        ScheduleConflictException exception = new ScheduleConflictException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        String message = "Test inheritance";
        ScheduleConflictException exception = new ScheduleConflictException(message);

        assertTrue(exception instanceof RuntimeException);
    }
}