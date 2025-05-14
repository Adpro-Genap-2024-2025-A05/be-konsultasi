package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.BaseResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private HomeController homeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        homeController = new HomeController();
    }

    @Test
    void healthCheck_shouldReturnUpStatus() {
        ResponseEntity<BaseResponseDto<Map<String, String>>> response = homeController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Service is running", response.getBody().getMessage());
        assertEquals("UP", response.getBody().getData().get("status"));
        assertEquals("Back-End Konsultasi API", response.getBody().getData().get("service"));
    }

    @Test
    void healthCheck_shouldNotReturnNullResponse() {
        ResponseEntity<BaseResponseDto<Map<String, String>>> response = homeController.healthCheck();

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }
}