package com.exam.util;

import javax.servlet.http.HttpServletRequest;

public class AuthUtil {
    public static String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    public static boolean isAuthenticated(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return token != null && JwtUtil.validateToken(token);
    }
    
    public static boolean isAdmin(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null && JwtUtil.validateToken(token)) {
            String role = JwtUtil.getRoleFromToken(token);
            return "admin".equals(role);
        }
        return false;
    }
    
    public static int getUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null && JwtUtil.validateToken(token)) {
            return JwtUtil.getUserIdFromToken(token);
        }
        return -1;
    }
}

