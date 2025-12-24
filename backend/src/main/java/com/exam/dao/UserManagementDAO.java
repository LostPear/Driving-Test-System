package com.exam.dao;

import com.exam.dao.QuestionDAO;
import com.exam.dao.UserDAO;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.util.DBUtil;
import com.exam.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManagementDAO {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static List<User> getUsers(int page, int pageSize) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM users ORDER BY id LIMIT ? OFFSET ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getString("created_at"));
                users.add(user);
            }
            return users;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static int getUsersCount() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT COUNT(*) as total FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static User getUserById(int id) throws SQLException {
        return UserDAO.getUserById(id);
    }
    
    public static void updateUser(int id, String username, String email, String role) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "UPDATE users SET username = ?, email = ?, role = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, role);
            stmt.setInt(4, id);
            stmt.executeUpdate();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static void deleteUser(int id) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static User createUser(String username, String email, String password, String role) throws SQLException {
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
            String sql = "INSERT INTO users (username, email, password, role, created_at) VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, UserDAO.hashPassword(password));
            stmt.setString(4, role != null ? role : "user");
            
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                return UserDAO.getUserById(userId);
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Map<String, Object> getUserStats(int id) throws SQLException {
        // 先尝试从缓存获取
        if (RedisUtil.isAvailable()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedStats = RedisUtil.getUserStats(id, Map.class);
            if (cachedStats != null) {
                return cachedStats;
            }
        }
        
        // 缓存未命中，从数据库查询
        Connection conn = DBUtil.getConnection();
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 考试总数（只统计type='exam'的记录）
            String sql = "SELECT COUNT(*) as total FROM exams WHERE user_id = ? AND type = 'exam' AND submitted_at IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            int totalExams = 0;
            if (rs.next()) {
                totalExams = rs.getInt("total");
                stats.put("totalExams", totalExams);
            }
            
            // 通过次数（只统计type='exam'的记录）
            sql = "SELECT COUNT(*) as passed FROM exams WHERE user_id = ? AND type = 'exam' AND passed = 1";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("passedExams", rs.getInt("passed"));
            }
            
            // 平均分（只统计type='exam'的记录）
            sql = "SELECT AVG(score) as avgScore FROM exams WHERE user_id = ? AND type = 'exam' AND score IS NOT NULL";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            double avgScore = 0.0;
            if (rs.next()) {
                Object avgScoreObj = rs.getObject("avgScore");
                if (avgScoreObj != null) {
                    avgScore = rs.getDouble("avgScore");
                }
                stats.put("avgScore", avgScore);
            }
            
            // 最高分（只统计type='exam'的记录）
            sql = "SELECT MAX(score) as maxScore FROM exams WHERE user_id = ? AND type = 'exam' AND score IS NOT NULL";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("maxScore", rs.getObject("maxScore"));
            }
            
            // 统计练习题目数（只统计type='practice'且已提交的记录，不重复的题目ID）
            // 由于questions字段是JSON，我们需要解析它
            sql = "SELECT questions FROM exams WHERE user_id = ? AND type = 'practice' AND submitted_at IS NOT NULL";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            Set<Integer> questionIds = new HashSet<>();
            while (rs.next()) {
                String questionsJson = rs.getString("questions");
                if (questionsJson != null && !questionsJson.isEmpty()) {
                    try {
                        // 使用ObjectMapper解析JSON数组
                        @SuppressWarnings("unchecked")
                        List<Object> ids = mapper.readValue(questionsJson, List.class);
                        if (ids != null) {
                            for (Object idObj : ids) {
                                if (idObj instanceof Number) {
                                    questionIds.add(((Number) idObj).intValue());
                                } else if (idObj instanceof String) {
                                    try {
                                        questionIds.add(Integer.parseInt((String) idObj));
                                    } catch (NumberFormatException e) {
                                        // 忽略无效的ID
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 解析失败，忽略
                    }
                }
            }
            stats.put("practiceCount", questionIds.size());
            
            // 统计练习详情：答对和答错题目数（只统计type='practice'的记录）
            sql = "SELECT questions, answers FROM exams WHERE user_id = ? AND type = 'practice' AND submitted_at IS NOT NULL AND answers IS NOT NULL AND answers != '{}'";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            int correctCount = 0;
            int wrongCount = 0;
            
            while (rs.next()) {
                String questionsJson = rs.getString("questions");
                String answersJson = rs.getString("answers");
                
                if (questionsJson != null && !questionsJson.isEmpty() && 
                    answersJson != null && !answersJson.isEmpty() && !answersJson.equals("{}")) {
                    try {
                        // 解析题目ID列表
                        @SuppressWarnings("unchecked")
                        List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                        List<Integer> qIds = new ArrayList<>();
                        for (Object idObj : questionIdsObj) {
                            if (idObj instanceof Number) {
                                qIds.add(((Number) idObj).intValue());
                            } else if (idObj instanceof String) {
                                try {
                                    qIds.add(Integer.parseInt((String) idObj));
                                } catch (NumberFormatException e) {
                                    // 忽略无效的ID
                                }
                            }
                        }
                        
                        // 解析答案
                        @SuppressWarnings("unchecked")
                        Map<String, Object> answersMap = mapper.readValue(answersJson, Map.class);
                        Map<Integer, Integer> answers = new HashMap<>();
                        for (Map.Entry<String, Object> entry : answersMap.entrySet()) {
                            int questionId = Integer.parseInt(entry.getKey());
                            int answer;
                            if (entry.getValue() instanceof Integer) {
                                answer = (Integer) entry.getValue();
                            } else if (entry.getValue() instanceof Number) {
                                answer = ((Number) entry.getValue()).intValue();
                            } else {
                                answer = Integer.parseInt(entry.getValue().toString());
                            }
                            answers.put(questionId, answer);
                        }
                        
                        // 检查每个题目的答案是否正确
                        for (Integer questionId : qIds) {
                            Integer userAnswer = answers.get(questionId);
                            if (userAnswer != null) {
                                // 获取题目的正确答案
                                Question question = QuestionDAO.getQuestionById(questionId);
                                if (question != null) {
                                    boolean isCorrect = false;
                                    if ("multiple".equals(question.getType())) {
                                        // 多选题：检查答案是否在correctAnswers中
                                        List<Integer> correctAnswers = question.getCorrectAnswers();
                                        if (correctAnswers != null && correctAnswers.contains(userAnswer)) {
                                            isCorrect = true;
                                        }
                                    } else {
                                        // 单选题和判断题：直接比较
                                        if (userAnswer == question.getCorrectAnswer()) {
                                            isCorrect = true;
                                        }
                                    }
                                    
                                    if (isCorrect) {
                                        correctCount++;
                                    } else {
                                        wrongCount++;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 解析失败，忽略
                    }
                }
            }
            
            stats.put("practiceCorrect", correctCount);
            stats.put("practiceWrong", wrongCount);
            
            // 计算正确率
            int totalAnswered = correctCount + wrongCount;
            int accuracy = 0;
            if (totalAnswered > 0) {
                accuracy = (int) Math.round((double) correctCount / totalAnswered * 100);
            }
            stats.put("practiceAccuracy", accuracy);
            
            // 存入缓存
            if (RedisUtil.isAvailable()) {
                RedisUtil.setUserStats(id, stats);
            }
            
            return stats;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
}

