package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.dto.TokenVerificationResponseDto;
import id.ac.ui.cs.advprog.bekonsultasi.enums.Role;
import id.ac.ui.cs.advprog.bekonsultasi.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class TokenVerificationService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    public TokenVerificationResponseDto verifyToken(String token) {
        try {
            if (isTokenExpired(token)) {
                throw new AuthenticationException("Token has expired");
            }
            
            Claims claims = extractAllClaims(token);
            
            String email = extractUsername(token); 
            String userId = claims.get("id", String.class);
            String roleStr = claims.get("role", String.class);
            String userName = claims.get("name", String.class); // Added this line
            
            if (userId == null || roleStr == null) {
                throw new AuthenticationException("Invalid token: missing required claims");
            }
            
            if (!Role.contains(roleStr)) {
                throw new AuthenticationException("Invalid role in token: " + roleStr);
            }
            
            Role role = Role.valueOf(roleStr);
            
            long expiresIn = getRemainingTime(token);
            
            return TokenVerificationResponseDto.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .userName(userName) 
                    .role(role)
                    .expiresIn(expiresIn)
                    .build();
            
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException("Token has expired");
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
    
    public String getUserNameFromToken(String token) {
        TokenVerificationResponseDto verification = verifyToken(token);
        return verification.getUserName();
    }
    
    public void validateRole(String token, Role expectedRole) {
        Role userRole = getUserRoleFromToken(token);
        if (userRole != expectedRole) {
            throw new AuthenticationException("Access denied. Required role: " + expectedRole);
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