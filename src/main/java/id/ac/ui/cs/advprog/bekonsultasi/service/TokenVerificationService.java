package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import io.micrometer.core.instrument.Counter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenVerificationService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    private final Counter tokenVerificationCounter;
    private final Counter tokenVerificationFailureCounter;
    private final Counter authenticationErrorCounter;
    
    public TokenVerificationResponseDto verifyToken(String token) {
        log.info("Verifying JWT token");
        tokenVerificationCounter.increment();
        
        try {
            if (isTokenExpired(token)) {
                tokenVerificationFailureCounter.increment();
                log.warn("Token verification failed: Token has expired");
                throw new AuthenticationException("Error verifying token: Token has expired");
            }

            Claims claims = extractAllClaims(token);

            String email = extractUsername(token);
            String userId = claims.get("id", String.class);
            String roleStr = claims.get("role", String.class);
            String userName = claims.get("name", String.class);

            if (userId == null || roleStr == null) {
                tokenVerificationFailureCounter.increment();
                authenticationErrorCounter.increment();
                log.error("Token verification failed: Missing required claims - userId: {}, role: {}", 
                         userId != null ? "present" : "missing", 
                         roleStr != null ? "present" : "missing");
                throw new AuthenticationException("Error verifying token: Invalid token: missing required claims");
            }

            if (!Role.contains(roleStr)) {
                tokenVerificationFailureCounter.increment();
                authenticationErrorCounter.increment();
                log.error("Token verification failed: Invalid role in token: {}", roleStr);
                throw new AuthenticationException("Error verifying token: Invalid role in token: " + roleStr);
            }

            Role role = Role.valueOf(roleStr);
            long expiresIn = getRemainingTime(token);

            log.info("Token verification successful for user: {}, role: {}, expires in: {}ms", userId, role, expiresIn);

            return TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .userName(userName)
                    .role(role)
                    .expiresIn(expiresIn)
                    .build();

        } catch (ExpiredJwtException e) {
            tokenVerificationFailureCounter.increment();
            log.warn("Token verification failed: Token has expired - {}", e.getMessage());
            throw new AuthenticationException("Error verifying token: Token has expired");
        } catch (AuthenticationException e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            tokenVerificationFailureCounter.increment();
            authenticationErrorCounter.increment();
            log.error("Token verification failed with unexpected error: {}", e.getMessage(), e);
            throw new AuthenticationException("Error verifying token: " + e.getMessage());
        }
    }
    
    public UUID getUserIdFromToken(String token) {
        log.info("Extracting user ID from token");
        try {
            TokenVerificationResponseDto verification = verifyToken(token);
            UUID userId = UUID.fromString(verification.getUserId());
            log.info("Successfully extracted user ID: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage());
            throw e;
        }
    }
    
    public Role getUserRoleFromToken(String token) {
        log.info("Extracting user role from token");
        try {
            TokenVerificationResponseDto verification = verifyToken(token);
            Role role = verification.getRole();
            log.info("Successfully extracted user role: {}", role);
            return role;
        } catch (Exception e) {
            log.error("Failed to extract user role from token: {}", e.getMessage());
            throw e;
        }
    }
    
    public String getUserNameFromToken(String token) {
        log.info("Extracting user name from token");
        try {
            TokenVerificationResponseDto verification = verifyToken(token);
            String userName = verification.getUserName();
            log.info("Successfully extracted user name for user: {}", verification.getUserId());
            return userName;
        } catch (Exception e) {
            log.error("Failed to extract user name from token: {}", e.getMessage());
            throw e;
        }
    }
    
    public void validateRole(String token, Role expectedRole) {
        log.info("Validating user role against expected role: {}", expectedRole);
        try {
            Role userRole = getUserRoleFromToken(token);
            if (userRole != expectedRole) {
                log.warn("Role validation failed: User has role {} but expected {}", userRole, expectedRole);
                throw new AuthenticationException("Access denied. Required role: " + expectedRole);
            }
            log.info("Role validation successful: User has required role {}", expectedRole);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Role validation failed with error: {}", e.getMessage(), e);
            throw new AuthenticationException("Role validation failed: " + e.getMessage());
        }
    }
    
    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
    
    private long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingTime);
        } catch (ExpiredJwtException e) {
            return 0;
        }
    }
}