package org.example.gitmazonmasternode.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;
    private SecretKey SECRET_KEY;

    private final long EXPIRATION_TIME = 3600 * 1000;

    @PostConstruct
    public void init() {
        this.SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String accessToken) {
        return Jwts.builder()
            .setSubject(username)
            .claim("accessToken", accessToken)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SECRET_KEY)
            .compact();
    }

    public String extractUsername(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    public String extractAccessToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("accessToken", String.class);
    }

    public Claims validateToken(String token) throws JwtException {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Expired or invalid JWT token", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid token", e);
        }

    }
}

