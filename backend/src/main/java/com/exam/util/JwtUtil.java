package com.exam.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "exam_system_secret_key_for_jwt_token_generation_2024";
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    public static String generateToken(int userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    public static int getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return (Integer) claims.get("userId");
        }
        return -1;
    }
    
    public static String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return (String) claims.get("role");
        }
        return null;
    }
}

