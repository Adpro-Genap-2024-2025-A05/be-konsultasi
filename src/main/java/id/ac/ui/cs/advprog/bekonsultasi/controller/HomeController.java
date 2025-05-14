package id.ac.ui.cs.advprog.bekonsultasi.controller;

import id.ac.ui.cs.advprog.bekonsultasi.dto.BaseResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class HomeController {

    @GetMapping
    public ResponseEntity<BaseResponseDto<Map<String, String>>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Back-End Konsultasi API");

        return ResponseEntity.ok(BaseResponseDto.success(status, "Service is running"));
    }
}