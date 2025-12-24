package com.exam.dao;

import com.exam.model.Question;
import com.exam.util.DBUtil;
import com.exam.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private static final Logger logger = LogManager.getLogger(QuestionDAO.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static List<Question> getQuestions(String search, String type, String difficulty, int page, int pageSize) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE 1=1");
            List<String> params = new ArrayList<>();
            
            if (search != null && !search.isEmpty()) {
                sql.append(" AND question LIKE ?");
                params.add("%" + search + "%");
            }
            if (type != null && !type.isEmpty()) {
                sql.append(" AND type = ?");
                params.add(type);
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                sql.append(" AND difficulty = ?");
                params.add(difficulty);
            }
            
            sql.append(" ORDER BY id LIMIT ? OFFSET ?");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            // 设置搜索、类型和难度参数
            if (search != null && !search.isEmpty()) {
                stmt.setString(paramIndex++, "%" + search + "%");
            }
            if (type != null && !type.isEmpty()) {
                stmt.setString(paramIndex++, type);
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                stmt.setString(paramIndex++, difficulty);
            }
            
            // 设置分页参数（使用setInt）
            stmt.setInt(paramIndex++, pageSize);
            stmt.setInt(paramIndex++, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            List<Question> questions = new ArrayList<>();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            
            return questions;
        } catch (SQLException e) {
            logger.error("Error in getQuestions method", e);
            throw e;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Question getQuestionById(int id) throws SQLException {
        // 先尝试从缓存获取
        if (RedisUtil.isAvailable()) {
            Question cachedQuestion = RedisUtil.getQuestion(id, Question.class);
            if (cachedQuestion != null) {
                return cachedQuestion;
            }
        }
        
        // 缓存未命中，从数据库查询
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM questions WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Question question = mapResultSetToQuestion(rs);
                
                // 存入缓存
                if (RedisUtil.isAvailable() && question != null) {
                    RedisUtil.setQuestion(id, question);
                }
                
                return question;
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Question createQuestion(Question question) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "INSERT INTO questions (type, difficulty, question, image, options, correct_answer, correct_answers, explanation, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, question.getType());
            stmt.setString(2, question.getDifficulty());
            stmt.setString(3, question.getQuestion());
            stmt.setString(4, question.getImage());
            stmt.setString(5, mapper.writeValueAsString(question.getOptions()));
            
            // 处理正确答案：如果是多选题，使用correct_answers；否则使用correct_answer
            if ("multiple".equals(question.getType()) && question.getCorrectAnswers() != null && !question.getCorrectAnswers().isEmpty()) {
                stmt.setInt(6, question.getCorrectAnswers().get(0)); // 向后兼容
                stmt.setString(7, mapper.writeValueAsString(question.getCorrectAnswers()));
            } else {
                stmt.setInt(6, question.getCorrectAnswer());
                stmt.setString(7, mapper.writeValueAsString(new int[]{question.getCorrectAnswer()})); // 转换为数组格式
            }
            
            stmt.setString(8, question.getExplanation());
            
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return getQuestionById(generatedKeys.getInt(1));
            }
            return null;
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static void updateQuestion(int id, Question question) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "UPDATE questions SET type = ?, difficulty = ?, question = ?, image = ?, options = ?, correct_answer = ?, correct_answers = ?, explanation = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, question.getType());
            stmt.setString(2, question.getDifficulty());
            stmt.setString(3, question.getQuestion());
            stmt.setString(4, question.getImage());
            stmt.setString(5, mapper.writeValueAsString(question.getOptions()));
            
            // 处理正确答案：如果是多选题，使用correct_answers；否则使用correct_answer
            if ("multiple".equals(question.getType()) && question.getCorrectAnswers() != null && !question.getCorrectAnswers().isEmpty()) {
                stmt.setInt(6, question.getCorrectAnswers().get(0)); // 向后兼容
                stmt.setString(7, mapper.writeValueAsString(question.getCorrectAnswers()));
            } else {
                stmt.setInt(6, question.getCorrectAnswer());
                stmt.setString(7, mapper.writeValueAsString(new int[]{question.getCorrectAnswer()})); // 转换为数组格式
            }
            
            stmt.setString(8, question.getExplanation());
            stmt.setInt(9, id);
            stmt.executeUpdate();
            
            // 清除题目缓存
            if (RedisUtil.isAvailable()) {
                RedisUtil.deleteQuestion(id);
            }
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static void deleteQuestion(int id) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "DELETE FROM questions WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static List<Question> getRandomQuestions(int count, String type) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql;
            PreparedStatement stmt;
            if (type != null && !type.isEmpty()) {
                sql = "SELECT * FROM questions WHERE type = ? ORDER BY RAND() LIMIT ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, type);
                stmt.setInt(2, count);
            } else {
                sql = "SELECT * FROM questions ORDER BY RAND() LIMIT ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, count);
            }
            
            ResultSet rs = stmt.executeQuery();
            List<Question> questions = new ArrayList<>();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            return questions;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static List<Question> getSequentialQuestions(int offset, int limit, String type) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql;
            PreparedStatement stmt;
            if (type != null && !type.isEmpty()) {
                sql = "SELECT * FROM questions WHERE type = ? ORDER BY id LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, type);
                stmt.setInt(2, limit);
                stmt.setInt(3, offset);
            } else {
                sql = "SELECT * FROM questions ORDER BY id LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
            }
            
            ResultSet rs = stmt.executeQuery();
            List<Question> questions = new ArrayList<>();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            return questions;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static int getQuestionsCount(String search, String type, String difficulty) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total FROM questions WHERE 1=1");
            
            PreparedStatement stmt;
            if (search != null && !search.isEmpty()) {
                sql.append(" AND question LIKE ?");
            }
            if (type != null && !type.isEmpty()) {
                sql.append(" AND type = ?");
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                sql.append(" AND difficulty = ?");
            }
            
            stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            if (search != null && !search.isEmpty()) {
                stmt.setString(paramIndex++, "%" + search + "%");
            }
            if (type != null && !type.isEmpty()) {
                stmt.setString(paramIndex++, type);
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                stmt.setString(paramIndex++, difficulty);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static int getQuestionsCount() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT COUNT(*) as total FROM questions";
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
    
    public static int batchCreateQuestions(List<Question> questions) throws SQLException {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        
        Connection conn = DBUtil.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO questions (type, difficulty, question, image, options, correct_answer, correct_answers, explanation, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            stmt = conn.prepareStatement(sql);
            
            int successCount = 0;
            for (Question question : questions) {
                try {
                    stmt.setString(1, question.getType());
                    stmt.setString(2, question.getDifficulty());
                    stmt.setString(3, question.getQuestion());
                    stmt.setString(4, question.getImage());
                    stmt.setString(5, mapper.writeValueAsString(question.getOptions()));
                    
                    // 处理正确答案：如果是多选题，使用correct_answers；否则使用correct_answer
                    if ("multiple".equals(question.getType()) && question.getCorrectAnswers() != null && !question.getCorrectAnswers().isEmpty()) {
                        stmt.setInt(6, question.getCorrectAnswers().get(0)); // 向后兼容
                        stmt.setString(7, mapper.writeValueAsString(question.getCorrectAnswers()));
                    } else {
                        stmt.setInt(6, question.getCorrectAnswer());
                        stmt.setString(7, mapper.writeValueAsString(new int[]{question.getCorrectAnswer()})); // 转换为数组格式
                    }
                    
                    stmt.setString(8, question.getExplanation() != null ? question.getExplanation() : "");
                    stmt.addBatch();
                    successCount++;
                } catch (Exception e) {
                    // 跳过无效的题目
                    continue;
                }
            }
            
            if (successCount > 0) {
                stmt.executeBatch();
            }
            
            return successCount;
        } catch (Exception e) {
            throw new SQLException(e);
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
    
    public static Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        try {
            Question question = new Question();
            question.setId(rs.getInt("id"));
            question.setType(rs.getString("type"));
            question.setDifficulty(rs.getString("difficulty"));
            question.setQuestion(rs.getString("question"));
            question.setImage(rs.getString("image"));
            
            // 处理JSON字段
            String optionsJson = rs.getString("options");
            if (optionsJson != null) {
                @SuppressWarnings("unchecked")
                List<String> options = mapper.readValue(optionsJson, List.class);
                question.setOptions(options);
            }
            
            question.setCorrectAnswer(rs.getInt("correct_answer"));
            
            // 处理correct_answers字段（多选题）
            try {
                String correctAnswersJson = rs.getString("correct_answers");
                if (correctAnswersJson != null && !correctAnswersJson.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Integer> correctAnswers = mapper.readValue(correctAnswersJson, List.class);
                    question.setCorrectAnswers(correctAnswers);
                } else if ("multiple".equals(question.getType())) {
                    // 如果没有correct_answers字段，但类型是multiple，使用correct_answer作为数组
                    List<Integer> correctAnswers = new ArrayList<>();
                    correctAnswers.add(rs.getInt("correct_answer"));
                    question.setCorrectAnswers(correctAnswers);
                }
            } catch (Exception e) {
                // 如果解析失败，使用默认值
                if ("multiple".equals(question.getType())) {
                    List<Integer> correctAnswers = new ArrayList<>();
                    correctAnswers.add(rs.getInt("correct_answer"));
                    question.setCorrectAnswers(correctAnswers);
                }
            }
            
            question.setExplanation(rs.getString("explanation"));
            question.setCreatedAt(rs.getString("created_at"));
            return question;
        } catch (Exception e) {
            throw new SQLException("Error mapping question: " + e.getMessage(), e);
        }
    }
}

