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
    
    /**
     * 创建考试（仅用于兼容，实际已不再使用）
     * 新版本中，考试在提交时一次性创建
     */
    @Deprecated
    public static Exam createExam(int userId, List<Integer> questionIds, String type) throws SQLException {
        // 此方法在新结构中已不再使用，因为考试是提交时一次性创建的
        // 为了向后兼容，返回一个临时Exam对象
        Exam exam = new Exam();
        exam.setUserId(userId);
        exam.setType(type != null ? type : "exam");
        exam.setAnswers(new HashMap<>());
        exam.setPassed(false);
        
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
    
    /**
     * 根据ID获取考试记录（使用JSON字段快速查询）
     */
    public static Exam getExamById(int id, int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM exam_records WHERE id = ? AND user_id = ?";
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
    
    /**
     * 提交考试或练习（仅使用JSON字段，不维护关联表以提升性能）
     */
    public static Exam submitExam(int userId, List<Integer> questionIds, String type, Map<Integer, Integer> answers) throws SQLException {
        Connection conn = DBUtil.getConnection();
        int recordId = 0;
        try {
            conn.setAutoCommit(false); // 开启事务
            
            // 计算分数和正确性
            int score = 0;
            int correctCount = 0;
            int wrongCount = 0;
            int total = questionIds.size();
            
            for (Integer questionId : questionIds) {
                Question q = QuestionDAO.getQuestionById(questionId);
                if (q != null) {
                    Integer userAnswer = answers.get(questionId);
                    if (userAnswer != null) {
                        boolean isCorrect = false;
                        if ("multiple".equals(q.getType())) {
                            List<Integer> correctAnswersList = q.getCorrectAnswers();
                            if (correctAnswersList != null && correctAnswersList.contains(userAnswer)) {
                                isCorrect = true;
                            }
                        } else {
                            if (userAnswer.equals(q.getCorrectAnswer())) {
                                isCorrect = true;
                            }
                        }
                        if (isCorrect) {
                            score++;
                            correctCount++;
                        } else {
                            wrongCount++;
                        }
                    } else {
                        wrongCount++;
                    }
                }
            }
            
            boolean passed = score >= (total * 0.9); // 90分及格
            
            // 转换为JSON格式
            String questionsJson = mapper.writeValueAsString(questionIds);
            String answersJson = mapper.writeValueAsString(answers);
            
            if ("exam".equals(type)) {
                // 插入考试记录（仅使用JSON字段，不维护关联表以提升性能）
                String sql = "INSERT INTO exam_records (user_id, questions, answers, score, total_questions, passed, created_at, submitted_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, userId);
                stmt.setString(2, questionsJson);
                stmt.setString(3, answersJson);
                stmt.setInt(4, score);
                stmt.setInt(5, total);
                stmt.setBoolean(6, passed);
                stmt.executeUpdate();
                
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    recordId = generatedKeys.getInt(1);
                }
                stmt.close();
                
                // 注意：不维护关联表（exam_record_questions和exam_record_answers）
                // 关联表仅用于ER图展示，实际运行时使用JSON字段查询，性能更好
                
                conn.commit();
            } else {
                // 插入练习记录（仅使用JSON字段，不维护关联表以提升性能）
                String sql = "INSERT INTO practice_records (user_id, questions, answers, total_questions, correct_count, wrong_count, created_at, submitted_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, userId);
                stmt.setString(2, questionsJson);
                stmt.setString(3, answersJson);
                stmt.setInt(4, total);
                stmt.setInt(5, correctCount);
                stmt.setInt(6, wrongCount);
                stmt.executeUpdate();
                
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    recordId = generatedKeys.getInt(1);
                }
                stmt.close();
                
                // 注意：不维护关联表（practice_record_questions和practice_record_answers）
                // 关联表仅用于ER图展示，实际运行时使用JSON字段查询，性能更好
                
                conn.commit();
            }
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore
                }
            }
            throw new SQLException("提交失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Ignore
                }
                DBUtil.closeConnection(conn);
            }
        }
        
        // 使用JSON字段快速查询返回
        if ("exam".equals(type)) {
            return getExamById(recordId, userId);
        } else {
            return getPracticeById(recordId, userId);
        }
    }
    
    /**
     * 获取练习记录（使用JSON字段快速查询）
     */
    private static Exam getPracticeById(int id, int userId) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM practice_records WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPractice(rs);
            }
            return null;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * 获取考试结果
     */
    public static Exam getExamResult(int id, int userId) throws SQLException {
        return getExamById(id, userId);
    }
    
    /**
     * 获取考试历史记录（使用JSON字段快速查询）
     */
    public static List<Exam> getExamHistory(int userId, int page, int pageSize) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT * FROM exam_records WHERE user_id = ? AND submitted_at IS NOT NULL ORDER BY submitted_at DESC LIMIT ? OFFSET ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            List<Exam> exams = new ArrayList<>();
            while (rs.next()) {
                exams.add(mapResultSetToExamLightweight(rs));
            }
            return exams;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * 轻量级映射：仅用于列表查询，使用JSON字段快速解析
     */
    private static Exam mapResultSetToExamLightweight(ResultSet rs) throws SQLException {
        try {
            Exam exam = new Exam();
            exam.setId(rs.getInt("id"));
            exam.setUserId(rs.getInt("user_id"));
            exam.setType("exam");
            exam.setScore(rs.getObject("score") != null ? rs.getInt("score") : null);
            exam.setPassed(rs.getBoolean("passed"));
            exam.setCreatedAt(rs.getString("created_at"));
            exam.setSubmittedAt(rs.getString("submitted_at"));
            
            // 解析题目ID列表（从JSON字段）
            String questionsJson = rs.getString("questions");
            List<Question> questions = new ArrayList<>();
            if (questionsJson != null && !questionsJson.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                for (Object id : questionIdsObj) {
                    if (id instanceof Integer) {
                        Question q = new Question();
                        q.setId((Integer) id);
                        questions.add(q);
                    } else if (id instanceof Number) {
                        Question q = new Question();
                        q.setId(((Number) id).intValue());
                        questions.add(q);
                    }
                }
            }
            exam.setQuestions(questions);
            exam.setAnswers(new HashMap<>()); // 列表查询不需要答案详情
            
            return exam;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    /**
     * 完整映射：使用JSON字段快速加载题目和答案
     */
    private static Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        try {
            Exam exam = new Exam();
            exam.setId(rs.getInt("id"));
            exam.setUserId(rs.getInt("user_id"));
            exam.setType("exam");
            exam.setScore(rs.getObject("score") != null ? rs.getInt("score") : null);
            exam.setPassed(rs.getBoolean("passed"));
            exam.setCreatedAt(rs.getString("created_at"));
            exam.setSubmittedAt(rs.getString("submitted_at"));
            
            // 解析题目ID列表（从JSON字段）
            String questionsJson = rs.getString("questions");
            List<Integer> questionIds = new ArrayList<>();
            if (questionsJson != null && !questionsJson.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                for (Object id : questionIdsObj) {
                    if (id instanceof Integer) {
                        questionIds.add((Integer) id);
                    } else if (id instanceof Number) {
                        questionIds.add(((Number) id).intValue());
                    }
                }
            }
            
            // 加载题目详情
            List<Question> questions = new ArrayList<>();
            for (Integer qId : questionIds) {
                Question q = QuestionDAO.getQuestionById(qId);
                if (q != null) {
                    questions.add(q);
                }
            }
            exam.setQuestions(questions);
            
            // 解析答案（从JSON字段）
            String answersJson = rs.getString("answers");
            Map<Integer, Integer> answers = new HashMap<>();
            if (answersJson != null && !answersJson.isEmpty() && !answersJson.equals("{}")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> answersMap = mapper.readValue(answersJson, Map.class);
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
            }
            exam.setAnswers(answers);
            
            return exam;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    /**
     * 映射练习记录（使用JSON字段快速查询）
     */
    private static Exam mapResultSetToPractice(ResultSet rs) throws SQLException {
        try {
            Exam exam = new Exam();
            exam.setId(rs.getInt("id"));
            exam.setUserId(rs.getInt("user_id"));
            exam.setType("practice");
            exam.setScore(rs.getObject("correct_count") != null ? rs.getInt("correct_count") : null);
            exam.setPassed(false);
            exam.setCreatedAt(rs.getString("created_at"));
            exam.setSubmittedAt(rs.getString("submitted_at"));
            
            // 解析题目ID列表（从JSON字段）
            String questionsJson = rs.getString("questions");
            List<Integer> questionIds = new ArrayList<>();
            if (questionsJson != null && !questionsJson.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Object> questionIdsObj = mapper.readValue(questionsJson, List.class);
                for (Object id : questionIdsObj) {
                    if (id instanceof Integer) {
                        questionIds.add((Integer) id);
                    } else if (id instanceof Number) {
                        questionIds.add(((Number) id).intValue());
                    }
                }
            }
            
            // 加载题目详情
            List<Question> questions = new ArrayList<>();
            for (Integer qId : questionIds) {
                Question q = QuestionDAO.getQuestionById(qId);
                if (q != null) {
                    questions.add(q);
                }
            }
            exam.setQuestions(questions);
            
            // 解析答案（从JSON字段）
            String answersJson = rs.getString("answers");
            Map<Integer, Integer> answers = new HashMap<>();
            if (answersJson != null && !answersJson.isEmpty() && !answersJson.equals("{}")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> answersMap = mapper.readValue(answersJson, Map.class);
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
            }
            exam.setAnswers(answers);
            
            return exam;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    /**
     * 获取今日考试数量
     */
    public static int getTodayExamsCount() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT COUNT(*) as total FROM exam_records WHERE DATE(submitted_at) = CURDATE() AND submitted_at IS NOT NULL";
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
    
    /**
     * 获取通过率
     */
    public static double getPassRate() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN passed = 1 THEN 1 ELSE 0 END) as passed FROM exam_records WHERE submitted_at IS NOT NULL";
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
