package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenVerificationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${auth.service.url}")
    private String authServiceUrl;
    
    public TokenVerificationResponseDto verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<TokenVerificationResponseDto> response = restTemplate.exchange(
                    authServiceUrl + "/verify",
                    HttpMethod.POST,
                    entity,
                    TokenVerificationResponseDto.class
            );
            
            TokenVerificationResponseDto result = response.getBody();
            if (result == null || !result.isValid()) {
                throw new AuthenticationException("Invalid or expired token");
            }
            
            return result;
        } catch (Exception e) {
            throw new AuthenticationException("Error verifying token: " + e.getMessage());
        }
    }
    
    public UUID getUserIdFromToken(String token) {
        TokenVerificationResponseDto verification = verifyToken(token);
        return UUID.fromString(verification.getUserId());
    }
    
    public Role getUserRoleFromToken(String token) {
        TokenVerificationResponseDto verification = verifyToken(token);
        return verification.getRole();
    }
    
    public void validateRole(String token, Role expectedRole) {
        Role userRole = getUserRoleFromToken(token);
        if (userRole != expectedRole) {
            throw new AuthenticationException("Access denied. Required role: " + expectedRole);
        }
    }
}