package id.ac.ui.cs.advprog.bekonsultasi.exception;

import id.ac.ui.cs.advprog.bekonsultasi.dto.BaseResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponseDto<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation error: ");
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMsg = error.getDefaultMessage();
            errorMessage.append(fieldName).append(" - ").append(errorMsg).append("; ");
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponseDto.error(
                        HttpStatus.BAD_REQUEST.value(),
                        errorMessage.toString().trim()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDto<String>> handleAllExceptions(Exception ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponseDto.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred: " + ex.getMessage()
                ));
    }
}