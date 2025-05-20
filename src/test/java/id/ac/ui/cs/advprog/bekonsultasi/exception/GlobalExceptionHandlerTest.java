package id.ac.ui.cs.advprog.bekonsultasi.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleValidationExceptions_shouldReturnFormattedErrorDetails() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("testObject", "field1", "Field 1 error"));
        fieldErrors.add(new FieldError("testObject", "field2", "Field 2 error"));

        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(validationException, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("Please check the input fields", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        
        assertNotNull(response.getBody().getDetails());
        assertTrue(response.getBody().getDetails() instanceof Map);
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertEquals(2, details.size());
        assertEquals("Field 1 error", details.get("field1"));
        assertEquals("Field 2 error", details.get("field2"));
    }

    @Test
    void handleValidationExceptions_shouldHandleEmptyErrors() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(validationException, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("Please check the input fields", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getDetails());
        assertTrue(response.getBody().getDetails() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody().getDetails()).isEmpty());
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void handleAllExceptions_shouldIncludeExceptionMessageInResponse() {
        String customErrorMessage = "Custom error details";
        Exception ex = new IllegalStateException(customErrorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Server Error", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains(customErrorMessage));
        assertEquals("/api/test", response.getBody().getPath());
        assertNull(response.getBody().getDetails());
    }
    
    @Test
    void handleAuthenticationException_shouldReturnUnauthorizedError() {
        String errorMessage = "Authentication failed";
        AuthenticationException ex = new AuthenticationException(errorMessage);
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, webRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Authentication Error", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getDetails());
    }
    
    @Test
    void handleScheduleException_shouldReturnBadRequestError() {
        String errorMessage = "Schedule error";
        ScheduleException ex = new ScheduleException(errorMessage);
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleScheduleException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Schedule Error", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getDetails());
    }
    
    @Test
    void handleScheduleConflictException_shouldReturnConflictError() {
        String errorMessage = "Schedule conflict";
        ScheduleConflictException ex = new ScheduleConflictException(errorMessage);
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleScheduleConflictException(ex, webRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Schedule Conflict", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getDetails());
    }
}