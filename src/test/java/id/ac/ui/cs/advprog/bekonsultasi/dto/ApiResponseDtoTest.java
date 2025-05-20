package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseDtoTest {

    @Test
    void success_shouldCreateSuccessResponseWithData() {
        String testData = "Test data";

        ApiResponseDto<String> response = ApiResponseDto.success(200, "Success", testData);

        assertEquals(200, response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_shouldCreateSuccessResponseWithStatusAndCustomMessage() {
        String testData = "Test data";
        String customMessage = "Custom success message";

        ApiResponseDto<String> response = ApiResponseDto.success(204, customMessage, testData);

        assertEquals(204, response.getStatus());
        assertEquals(customMessage, response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_shouldCreateCreatedResponseWithData() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", 1);
        testData.put("name", "Test");

        ApiResponseDto<Map<String, Object>> response = ApiResponseDto.success(201, "Created successfully", testData);

        assertEquals(201, response.getStatus());
        assertEquals("Created successfully", response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_shouldCreateErrorResponseWithNullData() {
        int statusCode = 400;
        String errorMessage = "Bad request error";

        ApiResponseDto<Object> response = ApiResponseDto.error(statusCode, errorMessage);

        assertEquals(statusCode, response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void constructor_shouldCreateResponseWithAllFields() {
        int status = 200;
        String message = "Test message";
        ZonedDateTime timestamp = ZonedDateTime.now();
        String data = "Test data";

        ApiResponseDto<String> response = new ApiResponseDto<>(status, message, timestamp, data);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals(data, response.getData());
    }

    @Test
    void builder_shouldCreateResponseWithAllFields() {
        int status = 200;
        String message = "Test message";
        ZonedDateTime timestamp = ZonedDateTime.now();
        String data = "Test data";

        ApiResponseDto<String> response = ApiResponseDto.<String>builder()
                .status(status)
                .message(message)
                .timestamp(timestamp)
                .data(data)
                .build();

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals(data, response.getData());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        ApiResponseDto<String> response = new ApiResponseDto<>();
        int status = 200;
        String message = "Test message";
        ZonedDateTime timestamp = ZonedDateTime.now();
        String data = "Test data";

        response.setStatus(status);
        response.setMessage(message);
        response.setTimestamp(timestamp);
        response.setData(data);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals(data, response.getData());
    }

    @Test
    void equalsAndHashCode_shouldWorkCorrectly() {
        ZonedDateTime timestamp = ZonedDateTime.now();
        ApiResponseDto<String> response1 = new ApiResponseDto<>(200, "Test", timestamp, "Data");
        ApiResponseDto<String> response2 = new ApiResponseDto<>(200, "Test", timestamp, "Data");
        ApiResponseDto<String> response3 = new ApiResponseDto<>(400, "Error", timestamp, null);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }
}