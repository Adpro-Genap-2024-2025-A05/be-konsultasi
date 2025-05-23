package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenVerificationServiceTest {

    private TokenVerificationService tokenVerificationService;
    private final String testSecret = "0000000000000000000000000000000000000000000000000000000000000000";
    private final UUID testUserId = UUID.randomUUID();
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        tokenVerificationService = new TokenVerificationService();
        ReflectionTestUtils.setField(tokenVerificationService, "secretKey", testSecret);
    }

    private String generateValidToken(Role role) {
        return generateToken(testUserId.toString(), testEmail, role.name(), 3600000);
    }

    private String generateExpiredToken(Role role) {
        return generateToken(testUserId.toString(), testEmail, role.name(), -10000);
    }

    private String generateTokenWithMissingClaims() {
        Map<String, Object> claims = new HashMap<>();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);
        
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(testEmail)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithInvalidRole() {
        return generateToken(testUserId.toString(), testEmail, "INVALID_ROLE", 3600000);
    }

    private String generateToken(String userId, String email, String role, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", role);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Nested
    class VerifyTokenTests {
        @Test
        void testVerifyToken_Success() {
            String token = generateValidToken(Role.CAREGIVER);
            
            TokenVerificationResponseDto result = tokenVerificationService.verifyToken(token);
            
            assertNotNull(result);
            assertTrue(result.isValid());
            assertEquals(testUserId.toString(), result.getUserId());
            assertEquals(testEmail, result.getEmail());
            assertEquals(Role.CAREGIVER, result.getRole());
            assertTrue(result.getExpiresIn() > 0);
        }
        
        @Test
        void testVerifyToken_ExpiredToken() {
            String token = generateExpiredToken(Role.CAREGIVER);
            
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });
            
            assertEquals("Error verifying token: Token has expired", exception.getMessage());
        }
        
        @Test
        void testVerifyToken_InvalidToken() {
            String token = "invalid.token.format";
            
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });
            
            assertTrue(exception.getMessage().contains("Error verifying token"));
        }
        
        @Test
        void testVerifyToken_MissingClaims() {
            String token = generateTokenWithMissingClaims();
            
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });
            
            assertEquals("Error verifying token: Invalid token: missing required claims", exception.getMessage());
        }
        
        @Test
        void testVerifyToken_InvalidRole() {
            String token = generateTokenWithInvalidRole();
            
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.verifyToken(token);
            });
            
            assertTrue(exception.getMessage().contains("Invalid role in token"));
        }
    }

    @Nested
    class TokenInformationExtractionTests {
        @Test
        void testGetUserIdFromToken() {
            String token = generateValidToken(Role.CAREGIVER);
            
            UUID result = tokenVerificationService.getUserIdFromToken(token);
            
            assertEquals(testUserId, result);
        }

        @Test
        void testGetUserRoleFromToken() {
            String token = generateValidToken(Role.PACILIAN);
            
            Role result = tokenVerificationService.getUserRoleFromToken(token);
            
            assertEquals(Role.PACILIAN, result);
        }
    }

    @Nested
    class RoleValidationTests {
        @Test
        void testValidateRole_Success() {
            String token = generateValidToken(Role.CAREGIVER);
            
            tokenVerificationService.validateRole(token, Role.CAREGIVER);
        }

        @Test
        void testValidateRole_WrongRole() {
            String token = generateValidToken(Role.PACILIAN);
            
            Exception exception = assertThrows(AuthenticationException.class, () -> {
                tokenVerificationService.validateRole(token, Role.CAREGIVER);
            });
            
            assertEquals("Access denied. Required role: CAREGIVER", exception.getMessage());
        }
    }
}