package com.exam.servlet;

import com.exam.dao.ExamDAO;
import com.exam.dao.QuestionDAO;
import com.exam.dao.UserDAO;
import com.exam.dao.UserManagementDAO;
import com.exam.model.User;
import com.exam.util.AuthUtil;
import com.exam.util.JsonUtil;
import com.exam.util.RedisUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserManagementServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (!AuthUtil.isAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取用户列表需要管理员权限
                if (!AuthUtil.isAdmin(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\": \"Forbidden\"}");
                    return;
                }
                handleGetUsers(request, response, out);
            } else if (pathInfo.equals("/stats/")) {
                // 获取管理员统计数据
                if (!AuthUtil.isAdmin(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\": \"Forbidden\"}");
                    return;
                }
                handleGetAdminStats(request, response, out);
            } else {
                // /{id}/ or /{id}/stats/
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[1]);
                        if (parts.length >= 3 && parts[2].equals("stats")) {
                            // 获取用户统计：管理员可以查看任何用户，普通用户只能查看自己的
                            int currentUserId = AuthUtil.getUserId(request);
                            if (!AuthUtil.isAdmin(request) && currentUserId != id) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                out.print("{\"error\": \"Forbidden\"}");
                                return;
                            }
                            handleGetUserStats(id, request, response, out);
                        } else {
                            // 获取单个用户信息需要管理员权限
                            if (!AuthUtil.isAdmin(request)) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                out.print("{\"error\": \"Forbidden\"}");
                                return;
                            }
                            handleGetUser(id, response, out);
                        }
                    } catch (NumberFormatException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Invalid user ID\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid path\"}");
                }
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
            if (!AuthUtil.isAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            
            if (!AuthUtil.isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Forbidden\"}");
                return;
            }
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                String idStr = pathInfo.replaceAll("/", "");
                try {
                    int id = Integer.parseInt(idStr);
                    handleUpdateUser(id, request, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid user ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing user ID\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (!AuthUtil.isAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            
            if (!AuthUtil.isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Forbidden\"}");
                return;
            }
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // 创建新用户
                handleCreateUser(request, response, out);
            } else if (pathInfo.matches("/\\d+/reset-password/")) {
                // /{id}/reset-password/
                String[] parts = pathInfo.split("/");
                try {
                    int id = Integer.parseInt(parts[1]);
                    handleResetPassword(id, request, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid user ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid path\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (!AuthUtil.isAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            
            if (!AuthUtil.isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Forbidden\"}");
                return;
            }
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                String idStr = pathInfo.replaceAll("/", "");
                try {
                    int id = Integer.parseInt(idStr);
                    UserManagementDAO.deleteUser(id);
                    out.print("{\"message\": \"User deleted successfully\"}");
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid user ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing user ID\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
        int pageSize = Integer.parseInt(request.getParameter("page_size") != null ? request.getParameter("page_size") : 
                                       (request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "20"));
        
        List<User> users = UserManagementDAO.getUsers(page, pageSize);
        int totalCount = UserManagementDAO.getUsersCount();
        
        // 为每个用户添加stats和isActive字段
        List<Map<String, Object>> userList = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("createdAt", user.getCreatedAt());
            userMap.put("isActive", true); // 默认激活状态
            
            // 获取用户统计信息
            Map<String, Object> stats = UserManagementDAO.getUserStats(user.getId());
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("practice", 0); // 练习题目数，可以从exam表统计
            userStats.put("exam", stats.get("totalExams") != null ? ((Number) stats.get("totalExams")).intValue() : 0);
            userStats.put("accuracy", stats.get("avgScore") != null ? ((Number) stats.get("avgScore")).intValue() : 0);
            userMap.put("stats", userStats);
            
            userList.add(userMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", userList);
        result.put("count", totalCount);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetUser(int id, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        User user = UserManagementDAO.getUserById(id);
        if (user != null) {
            out.print(JsonUtil.writeValueAsString(user));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"User not found\"}");
        }
    }
    
    private void handleUpdateUser(int id, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String username = data.get("username") != null ? data.get("username").toString() : null;
        String email = data.get("email") != null ? data.get("email").toString() : null;
        String role = data.get("role") != null ? data.get("role").toString() : null;
        
        UserManagementDAO.updateUser(id, username, email, role);
        User user = UserManagementDAO.getUserById(id);
        if (user != null) {
            out.print(JsonUtil.writeValueAsString(user));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"User not found\"}");
        }
    }
    
    private void handleGetUserStats(int id, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        Map<String, Object> stats = UserManagementDAO.getUserStats(id);
        
        // 转换为前端期望的格式
        Map<String, Object> result = new HashMap<>();
        result.put("practiceCount", stats.get("practiceCount") != null ? ((Number) stats.get("practiceCount")).intValue() : 0);
        result.put("examCount", stats.get("totalExams") != null ? ((Number) stats.get("totalExams")).intValue() : 0);
        
        // 计算正确率：使用做过的正确题目数/全部已经做过的题目数
        int practiceCorrect = stats.get("practiceCorrect") != null ? ((Number) stats.get("practiceCorrect")).intValue() : 0;
        int practiceWrong = stats.get("practiceWrong") != null ? ((Number) stats.get("practiceWrong")).intValue() : 0;
        int totalAnswered = practiceCorrect + practiceWrong;
        int accuracy = 0;
        if (totalAnswered > 0) {
            accuracy = (int) Math.round((double) practiceCorrect / totalAnswered * 100);
        }
        result.put("accuracy", accuracy);
        
        // 添加练习统计详细信息（用于Practice页面）
        result.put("practiceTotal", stats.get("practiceCount") != null ? ((Number) stats.get("practiceCount")).intValue() : 0);
        result.put("practiceCorrect", stats.get("practiceCorrect") != null ? ((Number) stats.get("practiceCorrect")).intValue() : 0);
        result.put("practiceWrong", stats.get("practiceWrong") != null ? ((Number) stats.get("practiceWrong")).intValue() : 0);
        result.put("practiceAccuracy", stats.get("practiceAccuracy") != null ? ((Number) stats.get("practiceAccuracy")).intValue() : 0);
        
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetAdminStats(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        // 先尝试从缓存获取
        if (RedisUtil.isAvailable()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedResult = RedisUtil.getAdminStats(Map.class);
            if (cachedResult != null) {
                out.print(JsonUtil.writeValueAsString(cachedResult));
                return;
            }
        }
        
        // 缓存未命中，从数据库查询
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        int totalUsers = UserManagementDAO.getUsersCount();
        stats.put("totalUsers", totalUsers);
        
        // 题库总数
        int totalQuestions = QuestionDAO.getQuestionsCount();
        stats.put("totalQuestions", totalQuestions);
        
        // 今日考试次数
        int todayExams = ExamDAO.getTodayExamsCount();
        stats.put("todayExams", todayExams);
        
        // 平均通过率
        double passRate = ExamDAO.getPassRate();
        stats.put("passRate", (int) Math.round(passRate));
        
        // 最近注册用户（最近5个）
        List<User> recentUsers = UserManagementDAO.getUsers(1, 5);
        List<Map<String, Object>> recentUsersList = new ArrayList<>();
        for (User user : recentUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("createdAt", user.getCreatedAt());
            recentUsersList.add(userMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("stats", stats);
        result.put("recentUsers", recentUsersList);
        
        // 存入缓存
        if (RedisUtil.isAvailable()) {
            RedisUtil.setAdminStats(result);
        }
        
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        String username = data.get("username") != null ? data.get("username").toString() : null;
        String email = data.get("email") != null ? data.get("email").toString() : null;
        String password = data.get("password") != null ? data.get("password").toString() : null;
        String confirmPassword = data.get("confirmPassword") != null ? data.get("confirmPassword").toString() : null;
        String role = data.get("role") != null ? data.get("role").toString() : "user";
        
        // 验证必填字段
        if (username == null || username.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"用户名不能为空\"}");
            return;
        }
        
        if (email == null || email.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"邮箱不能为空\"}");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"密码不能为空\"}");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"两次输入的密码不一致\"}");
            return;
        }
        
        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"密码长度至少为6个字符\"}");
            return;
        }
        
        // 验证角色
        if (!role.equals("user") && !role.equals("admin")) {
            role = "user";
        }
        
        try {
            User user = UserManagementDAO.createUser(username, email, password, role);
            if (user != null) {
                out.print(JsonUtil.writeValueAsString(user));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"创建用户失败\"}");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleResetPassword(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        // 检查用户是否存在
        User targetUser = UserManagementDAO.getUserById(userId);
        if (targetUser == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"用户不存在\"}");
            return;
        }
        
        // 直接重置密码为"password"，不需要验证原密码
        try {
            UserDAO.adminChangePassword(userId, "password");
            out.print("{\"message\": \"密码已重置为默认密码\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

