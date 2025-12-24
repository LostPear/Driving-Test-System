package com.exam.dao;

import com.exam.model.User;
import com.exam.util.DBUtil;
import com.exam.util.RedisUtil;
import java.sql.*;
import java.security.MessageDigest;

public class UserDAO {
    
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }
    
    public static User register(String username, String email, String password) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 检查用户名是否已存在
            String checkSql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            checkSql = "SELECT id FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("邮箱已存在");
            }
            
            // 插入新用户
            String sql = "INSERT INTO users (username, email, password, role, created_at) VALUES (?, ?, ?, 'user', NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));
            
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                return getUserById(userId);
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static User login(String username, String password) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null); // 不返回密码
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getString("created_at"));
                return user;
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static User getUserById(int id) throws SQLException {
        // 先尝试从缓存获取
        if (RedisUtil.isAvailable()) {
            User cachedUser = RedisUtil.getUser(id, User.class);
            if (cachedUser != null) {
                return cachedUser;
            }
        }
        
        // 缓存未命中，从数据库查询
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getString("created_at"));
                
                // 存入缓存
                if (RedisUtil.isAvailable()) {
                    RedisUtil.setUser(id, user);
                }
                
                return user;
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static void updateProfile(int id, String username, String email) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            
            // 清除用户缓存
            if (RedisUtil.isAvailable()) {
                RedisUtil.deleteUser(id);
            }
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static void changePassword(int id, String oldPassword, String newPassword) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 验证旧密码
            String sql = "SELECT password FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !rs.getString("password").equals(hashPassword(oldPassword))) {
                throw new SQLException("旧密码错误");
            }
            
            // 更新密码
            sql = "UPDATE users SET password = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    // 管理员直接更改用户密码（不需要验证旧密码）
    public static void adminChangePassword(int id, String newPassword) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("用户不存在");
            }
            
            // 清除用户缓存（密码变更不影响缓存，但为了一致性清除）
            if (RedisUtil.isAvailable()) {
                RedisUtil.deleteUser(id);
            }
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
}

