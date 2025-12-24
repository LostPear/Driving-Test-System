package com.exam.servlet;

import com.exam.dao.UserDAO;
import com.exam.model.User;
import com.exam.util.AuthUtil;
import com.exam.util.JsonUtil;
import com.exam.util.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AuthServlet.class);
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.warn("Auth request received with invalid path: {}", pathInfo);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid path\"}");
                return;
            }
            
            if (pathInfo.equals("/register/")) {
                handleRegister(request, response, out);
            } else if (pathInfo.equals("/login/")) {
                handleLogin(request, response, out);
            } else if (pathInfo.equals("/logout/")) {
                handleLogout(request, response, out);
            } else if (pathInfo.equals("/change-password/")) {
                handleChangePassword(request, response, out);
            } else {
            logger.warn("Auth request received with unknown path: {}", pathInfo);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Not found\"}");
        }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.equals("/profile/")) {
                handleUpdateProfile(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String username = data.get("username") != null ? data.get("username").toString() : null;
        String email = data.get("email") != null ? data.get("email").toString() : null;
        String password = data.get("password") != null ? data.get("password").toString() : null;
        
        if (username == null || email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Missing required fields\"}");
            return;
        }
        
        User user = UserDAO.register(username, email, password);
        if (user != null) {
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            result.put("user", userInfo);
            out.print(JsonUtil.writeValueAsString(result));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Registration failed\"}");
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String username = data.get("username") != null ? data.get("username").toString() : null;
        String password = data.get("password") != null ? data.get("password").toString() : null;
        
        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Missing username or password\"}");
            return;
        }
        
        User user = UserDAO.login(username, password);
        if (user != null) {
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            result.put("user", userInfo);
            out.print(JsonUtil.writeValueAsString(result));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Invalid username or password\"}");
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        // 客户端删除token即可，服务端无需操作
        out.print("{\"message\": \"Logged out successfully\"}");
    }
    
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        if (!AuthUtil.isAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        
        int userId = AuthUtil.getUserId(request);
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String username = data.get("username") != null ? data.get("username").toString() : null;
        String email = data.get("email") != null ? data.get("email").toString() : null;
        
        UserDAO.updateProfile(userId, username, email);
        User user = UserDAO.getUserById(userId);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        out.print(JsonUtil.writeValueAsString(userInfo));
    }
    
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        if (!AuthUtil.isAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        
        int userId = AuthUtil.getUserId(request);
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String oldPassword = data.get("oldPassword") != null ? data.get("oldPassword").toString() : null;
        String newPassword = data.get("newPassword") != null ? data.get("newPassword").toString() : null;
        
        if (oldPassword == null || newPassword == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Missing required fields\"}");
            return;
        }
        
        try {
            UserDAO.changePassword(userId, oldPassword, newPassword);
            out.print("{\"message\": \"Password changed successfully\"}");
        } catch (SQLException e) {
            logger.warn("Password change failed for user ID: {} - {}", userId, e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

