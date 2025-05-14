package id.ac.ui.cs.advprog.bekonsultasi.dto;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseResponseDtoTest {

    @Test
    void success_shouldCreateSuccessResponseWithData() {
        String testData = "Test data";

        BaseResponseDto<String> response = BaseResponseDto.success(testData);

        assertEquals(200, response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_shouldCreateSuccessResponseWithDataAndCustomMessage() {
        String testData = "Test data";
        String customMessage = "Custom success message";

        BaseResponseDto<String> response = BaseResponseDto.success(testData, customMessage);

        assertEquals(200, response.getStatus());
        assertEquals(customMessage, response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void created_shouldCreateCreatedResponseWithData() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", 1);
        testData.put("name", "Test");

        BaseResponseDto<Map<String, Object>> response = BaseResponseDto.created(testData);

        assertEquals(201, response.getStatus());
        assertEquals("Created successfully", response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_shouldCreateErrorResponseWithNullData() {
        int statusCode = 400;
        String errorMessage = "Bad request error";

        BaseResponseDto<Object> response = BaseResponseDto.error(statusCode, errorMessage);

        assertEquals(statusCode, response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void errorWithData_shouldCreateErrorResponseWithData() {
        int statusCode = 400;
        String errorMessage = "Bad request error";
        String errorData = "Error details";

        BaseResponseDto<String> response = BaseResponseDto.errorWithData(statusCode, errorMessage, errorData);

        assertEquals(statusCode, response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertEquals(errorData, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void constructor_shouldCreateResponseWithAllFields() {
        int status = 200;
        String message = "Test message";
        Date timestamp = new Date();
        String data = "Test data";

        BaseResponseDto<String> response = new BaseResponseDto<>(status, message, timestamp, data);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals(data, response.getData());
    }

    @Test
    void builder_shouldCreateResponseWithAllFields() {
        int status = 200;
        String message = "Test message";
        Date timestamp = new Date();
        String data = "Test data";

        BaseResponseDto<String> response = BaseResponseDto.<String>builder()
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
        BaseResponseDto<String> response = new BaseResponseDto<>();
        int status = 200;
        String message = "Test message";
        Date timestamp = new Date();
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
        Date timestamp = new Date();
        BaseResponseDto<String> response1 = new BaseResponseDto<>(200, "Test", timestamp, "Data");
        BaseResponseDto<String> response2 = new BaseResponseDto<>(200, "Test", timestamp, "Data");
        BaseResponseDto<String> response3 = new BaseResponseDto<>(400, "Error", timestamp, null);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }
}