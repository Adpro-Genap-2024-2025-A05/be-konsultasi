package id.ac.ui.cs.advprog.bekonsultasi.exception;

import id.ac.ui.cs.advprog.bekonsultasi.dto.BaseResponseDto;
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

import java.util.ArrayList;
import java.util.List;

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
    }

    @Test
    void handleValidationExceptions_shouldReturnFormattedErrorMessage() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("testObject", "field1", "Field 1 error"));
        fieldErrors.add(new FieldError("testObject", "field2", "Field 2 error"));

        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        ResponseEntity<BaseResponseDto<Object>> response = exceptionHandler.handleValidationExceptions(validationException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Validation error"));
        assertTrue(response.getBody().getMessage().contains("field1"));
        assertTrue(response.getBody().getMessage().contains("Field 1 error"));
        assertTrue(response.getBody().getMessage().contains("field2"));
        assertTrue(response.getBody().getMessage().contains("Field 2 error"));
        assertNull(response.getBody().getData());
    }

    @Test
    void handleValidationExceptions_shouldHandleEmptyErrors() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>());

        ResponseEntity<BaseResponseDto<Object>> response = exceptionHandler.handleValidationExceptions(validationException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation error:", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<BaseResponseDto<String>> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleAllExceptions_shouldIncludeExceptionMessageInResponse() {
        String customErrorMessage = "Custom error details";
        Exception ex = new IllegalStateException(customErrorMessage);

        ResponseEntity<BaseResponseDto<String>> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains(customErrorMessage));
        assertNull(response.getBody().getData());
    }
}