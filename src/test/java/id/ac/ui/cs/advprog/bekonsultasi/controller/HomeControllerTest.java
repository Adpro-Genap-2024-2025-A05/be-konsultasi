package id.ac.ui.cs.advprog.bekonsultasi.controller;

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
        ResponseEntity<Map<String, String>> response = homeController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("Back-End Konsultasi API", response.getBody().get("service"));
    }
    
    @Test
    void healthCheck_shouldNotReturnNullResponse() {
        ResponseEntity<Map<String, String>> response = homeController.healthCheck();

        assertNotNull(response);
        assertNotNull(response.getBody());
    }
}