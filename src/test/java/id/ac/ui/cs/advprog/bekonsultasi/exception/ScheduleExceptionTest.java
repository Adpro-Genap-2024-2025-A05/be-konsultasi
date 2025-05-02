package id.ac.ui.cs.advprog.bekonsultasi.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleExceptionTest {
    @Test
    void testConstructorWithMessage() {
        String message = "Test exception message";
        ScheduleException exception = new ScheduleException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        String message = "Test inheritance";
        ScheduleException exception = new ScheduleException(message);

        assertTrue(exception instanceof RuntimeException);
    }
}