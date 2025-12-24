package com.exam.dao;

import com.exam.model.Question;
import com.exam.util.DBUtil;
import com.exam.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static boolean addFavorite(int userId, int questionId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO favorites (user_id, question_id, created_at) VALUES (?, ?, NOW()) " +
                       "ON DUPLICATE KEY UPDATE created_at = NOW()";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, questionId);
            int rowsAffected = stmt.executeUpdate();
            
            // 清除收藏列表缓存
            if (RedisUtil.isAvailable() && rowsAffected > 0) {
                RedisUtil.deleteFavorites(userId);
            }
            
            return rowsAffected > 0;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
    
    public static boolean removeFavorite(int userId, int questionId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM favorites WHERE user_id = ? AND question_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, questionId);
            int rowsAffected = stmt.executeUpdate();
            
            // 清除收藏列表缓存
            if (RedisUtil.isAvailable() && rowsAffected > 0) {
                RedisUtil.deleteFavorites(userId);
            }
            
            return rowsAffected > 0;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
    
    public static boolean isFavorite(int userId, int questionId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT COUNT(*) as count FROM favorites WHERE user_id = ? AND question_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
    
    public static List<Question> getFavorites(int userId, int page, int pageSize) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT q.* FROM questions q " +
                       "INNER JOIN favorites f ON q.id = f.question_id " +
                       "WHERE f.user_id = ? " +
                       "ORDER BY f.created_at DESC " +
                       "LIMIT ? OFFSET ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            List<Question> questions = new ArrayList<>();
            
            while (rs.next()) {
                Question question = QuestionDAO.mapResultSetToQuestion(rs);
                questions.add(question);
            }
            
            return questions;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
    
    public static int getFavoritesCount(int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT COUNT(*) as total FROM favorites WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
    
    public static List<Integer> getFavoriteQuestionIds(int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT question_id FROM favorites WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Integer> questionIds = new ArrayList<>();
            
            while (rs.next()) {
                questionIds.add(rs.getInt("question_id"));
            }
            
            return questionIds;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            DBUtil.closeConnection(conn);
        }
    }
}

