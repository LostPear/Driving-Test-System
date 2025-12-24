package com.exam.dao;

import com.exam.model.Exam;
import com.exam.model.Question;
import com.exam.util.DBUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamDAO {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static Exam createExam(int userId, List<Integer> questionIds, String type) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 创建考试记录
            String sql = "INSERT INTO exams (user_id, type, questions, answers, created_at) VALUES (?, ?, ?, '{}', NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, type != null ? type : "exam");
            stmt.setString(3, mapper.writeValueAsString(questionIds));
            
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int examId = generatedKeys.getInt(1);
                Exam exam = new Exam();
                exam.setId(examId);
                exam.setUserId(userId);
                exam.setType(type != null ? type : "exam");
                exam.setAnswers(new HashMap<>());
                exam.setPassed(false);
                
                // 获取题目详情
                List<Question> questions = new ArrayList<>();
                for (Integer qId : questionIds) {
                    Question q = QuestionDAO.getQuestionById(qId);
                    if (q != null) {
                        questions.add(q);
                    }
                }
                exam.setQuestions(questions);
                
                return exam;
            }
            return null;
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Exam getExamById(int id, int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM exams WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToExam(rs);
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Exam submitExam(int userId, List<Integer> questionIds, String type, Map<Integer, Integer> answers) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 计算分数
            int score = 0;
            int total = questionIds.size();
            
            for (Integer questionId : questionIds) {
                Question q = QuestionDAO.getQuestionById(questionId);
                if (q != null) {
                    Integer userAnswer = answers.get(questionId);
                    if (userAnswer != null) {
                        boolean isCorrect = false;
                        if ("multiple".equals(q.getType())) {
                            // 多选题：检查答案是否在correctAnswers中
                            List<Integer> correctAnswersList = q.getCorrectAnswers();
                            if (correctAnswersList != null && correctAnswersList.contains(userAnswer)) {
                                isCorrect = true;
                            }
                        } else {
                            // 单选题和判断题
                            if (userAnswer == q.getCorrectAnswer()) {
                                isCorrect = true;
                            }
                        }
                        if (isCorrect) {
                    score++;
                        }
                    }
                }
            }
            
            boolean passed = score >= (total * 0.9); // 90分及格
            
            // 创建并提交考试记录（一次性完成）
            String sql = "INSERT INTO exams (user_id, type, questions, answers, score, passed, created_at, submitted_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, type != null ? type : "exam");
            stmt.setString(3, mapper.writeValueAsString(questionIds));
            stmt.setString(4, mapper.writeValueAsString(answers));
            stmt.setInt(5, score);
            stmt.setBoolean(6, passed);
            
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int examId = generatedKeys.getInt(1);
                return getExamById(examId, userId);
            }
            return null;
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static Exam getExamResult(int id, int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM exams WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToExam(rs);
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public static List<Exam> getExamHistory(int userId, int page, int pageSize) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 只查询type='exam'的记录，排除practice类型的记录
            String sql = "SELECT * FROM exams WHERE user_id = ? AND type = 'exam' AND submitted_at IS NOT NULL ORDER BY submitted_at DESC LIMIT ? OFFSET ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            List<Exam> exams = new ArrayList<>();
            while (rs.next()) {
                // 使用轻量级映射，不加载题目详情
                exams.add(mapResultSetToExamLightweight(rs));
            }
            return exams;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    // 轻量级映射方法：不加载题目详情，只保存题目ID列表，用于列表查询
    private static Exam mapResultSetToExamLightweight(ResultSet rs) throws SQLException {
        try {
            Exam exam = new Exam();
            exam.setId(rs.getInt("id"));
            exam.setUserId(rs.getInt("user_id"));
            exam.setType(rs.getString("type"));
            exam.setScore(rs.getObject("score") != null ? rs.getInt("score") : null);
            exam.setPassed(rs.getBoolean("passed"));
            exam.setCreatedAt(rs.getString("created_at"));
            exam.setSubmittedAt(rs.getString("submitted_at"));
            
            // 解析题目ID列表（不加载题目详情）
            String questionsJson = rs.getString("questions");
            if (questionsJson != null && !questionsJson.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                List<Integer> questionIds = new ArrayList<>();
                for (Object id : questionIdsObj) {
                    if (id instanceof Integer) {
                        questionIds.add((Integer) id);
                    } else if (id instanceof Number) {
                        questionIds.add(((Number) id).intValue());
                    }
                }
                // 创建空的Question列表，只保存ID信息（通过questions字段的JSON已经保存了ID）
                // 为了保持兼容性，创建一个包含ID的轻量级Question对象列表
                List<Question> questions = new ArrayList<>();
                for (Integer qId : questionIds) {
                    Question q = new Question();
                    q.setId(qId);
                    questions.add(q);
                }
                exam.setQuestions(questions);
            } else {
                exam.setQuestions(new ArrayList<>());
            }
            
            // 解析答案
            String answersJson = rs.getString("answers");
            if (answersJson != null && !answersJson.isEmpty() && !answersJson.equals("{}")) {
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
                exam.setAnswers(answers);
            } else {
                exam.setAnswers(new HashMap<>());
            }
            
            return exam;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    private static Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        try {
            Exam exam = new Exam();
            exam.setId(rs.getInt("id"));
            exam.setUserId(rs.getInt("user_id"));
            exam.setType(rs.getString("type"));
            exam.setScore(rs.getObject("score") != null ? rs.getInt("score") : null);
            exam.setPassed(rs.getBoolean("passed"));
            exam.setCreatedAt(rs.getString("created_at"));
            exam.setSubmittedAt(rs.getString("submitted_at"));
            
            // 解析题目ID列表
            String questionsJson = rs.getString("questions");
            if (questionsJson != null && !questionsJson.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                List<Integer> questionIds = new ArrayList<>();
                for (Object id : questionIdsObj) {
                    if (id instanceof Integer) {
                        questionIds.add((Integer) id);
                    } else if (id instanceof Number) {
                        questionIds.add(((Number) id).intValue());
                    }
                }
                List<Question> questions = new ArrayList<>();
                for (Integer qId : questionIds) {
                    Question q = QuestionDAO.getQuestionById(qId);
                    if (q != null) {
                        questions.add(q);
                    }
                }
                exam.setQuestions(questions);
            } else {
                exam.setQuestions(new ArrayList<>());
            }
            
            // 解析答案
            String answersJson = rs.getString("answers");
            if (answersJson != null && !answersJson.isEmpty() && !answersJson.equals("{}")) {
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
                exam.setAnswers(answers);
            } else {
                exam.setAnswers(new HashMap<>());
            }
            
            return exam;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    public static int getTodayExamsCount() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 只统计type='exam'的记录
            String sql = "SELECT COUNT(*) as total FROM exams WHERE type = 'exam' AND DATE(submitted_at) = CURDATE() AND submitted_at IS NOT NULL";
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
    
    public static double getPassRate() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 只统计type='exam'的记录
            String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN passed = 1 THEN 1 ELSE 0 END) as passed FROM exams WHERE type = 'exam' AND submitted_at IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int passed = rs.getInt("passed");
                if (total > 0) {
                    return (double) passed / total * 100;
                }
            }
            return 0.0;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
}

