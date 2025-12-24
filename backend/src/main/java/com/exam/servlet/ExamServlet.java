package com.exam.servlet;

import com.exam.dao.ExamDAO;
import com.exam.dao.QuestionDAO;
import com.exam.model.Exam;
import com.exam.model.Question;
import com.exam.util.AuthUtil;
import com.exam.util.JsonUtil;
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

@WebServlet("/api/exams/*")
public class ExamServlet extends HttpServlet {
    
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
            
            int userId = AuthUtil.getUserId(request);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid path\"}");
            } else if (pathInfo.equals("/history/")) {
                handleGetExamHistory(userId, request, response, out);
            } else {
                // /{id}/ or /{id}/result/
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[1]);
                        if (parts.length >= 3 && parts[2].equals("result")) {
                            handleGetExamResult(id, userId, response, out);
                        } else {
                            handleGetExam(id, userId, response, out);
                        }
                    } catch (NumberFormatException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Invalid exam ID\"}");
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
            
            int userId = AuthUtil.getUserId(request);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleCreateExam(userId, request, response, out);
            } else if (pathInfo.equals("/submit/")) {
                // 提交考试（不依赖exam ID，直接创建记录）
                handleSubmitExam(userId, request, response, out);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleCreateExam(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        // 获取type，默认为'exam'
        String type = "exam";
        if (data.containsKey("type") && data.get("type") != null) {
            type = String.valueOf(data.get("type"));
        }
        
        // 如果没有提供题目ID列表，则随机抽取100道题
        List<Integer> questionIds;
        if (data.containsKey("questionIds") && data.get("questionIds") != null) {
            @SuppressWarnings("unchecked")
            List<Object> ids = (List<Object>) data.get("questionIds");
            questionIds = new java.util.ArrayList<>();
            for (Object id : ids) {
                if (id instanceof Integer) {
                    questionIds.add((Integer) id);
                } else if (id instanceof Number) {
                    questionIds.add(((Number) id).intValue());
                }
            }
        } else {
            // 随机抽取100道题（不筛选类型）
            List<Question> randomQuestions = QuestionDAO.getRandomQuestions(100, null);
            questionIds = new java.util.ArrayList<>();
            for (Question q : randomQuestions) {
                questionIds.add(q.getId());
            }
        }
        
        // 只返回题目，不创建数据库记录
        List<Question> questions = new ArrayList<>();
        for (Integer qId : questionIds) {
            Question q = QuestionDAO.getQuestionById(qId);
            if (q != null) {
                questions.add(q);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetExam(int id, int userId, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        Exam exam = ExamDAO.getExamById(id, userId);
        if (exam != null) {
            out.print(JsonUtil.writeValueAsString(exam));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Exam not found\"}");
        }
    }
    
    // 新的提交方法：不依赖exam ID，直接创建记录
    private void handleSubmitExam(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Map<String, Object> data = JsonUtil.readValueAsMap(reader);
        
        // 获取题目ID列表和type
        @SuppressWarnings("unchecked")
        List<Object> questionIdsObj = (List<Object>) data.get("questionIds");
        List<Integer> questionIds = new ArrayList<>();
        for (Object idObj : questionIdsObj) {
            if (idObj instanceof Integer) {
                questionIds.add((Integer) idObj);
            } else if (idObj instanceof Number) {
                questionIds.add(((Number) idObj).intValue());
            }
        }
        
        String type = "exam";
        if (data.containsKey("type") && data.get("type") != null) {
            type = String.valueOf(data.get("type"));
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> answersMap = (Map<String, Object>) data.get("answers");
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
        
        // 创建并提交考试记录（一次性完成）
        Exam exam = ExamDAO.submitExam(userId, questionIds, type, answers);
        if (exam != null) {
            // 转换为前端期望的格式
            Map<String, Object> result = convertExamToResult(exam);
            out.print(JsonUtil.writeValueAsString(result));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to submit exam\"}");
        }
    }
    
    // 转换Exam对象为前端期望的格式
    private Map<String, Object> convertExamToResult(Exam exam) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", exam.getId());
        result.put("score", exam.getScore() != null ? exam.getScore() : 0);
        result.put("passed", exam.isPassed());
        
        // 计算统计信息
        int correct = 0;
        int wrong = 0;
        int unanswered = 0;
        List<Map<String, Object>> wrongQuestions = new ArrayList<>();
        
        for (Question q : exam.getQuestions()) {
            Integer userAnswer = exam.getAnswers().get(q.getId());
            if (userAnswer == null) {
                unanswered++;
            } else {
                boolean isCorrect = false;
                if ("multiple".equals(q.getType())) {
                    List<Integer> correctAnswers = q.getCorrectAnswers();
                    if (correctAnswers != null && correctAnswers.contains(userAnswer)) {
                        isCorrect = true;
                    }
                } else {
                    if (userAnswer == q.getCorrectAnswer()) {
                        isCorrect = true;
                    }
                }
                
                if (isCorrect) {
                    correct++;
        } else {
                    wrong++;
                    Map<String, Object> wrongQ = new HashMap<>();
                    wrongQ.put("id", q.getId());
                    wrongQ.put("question", q.getQuestion());
                    wrongQ.put("type", q.getType());
                    wrongQ.put("options", q.getOptions());
                    wrongQ.put("correctAnswer", q.getCorrectAnswer());
                    wrongQ.put("correctAnswers", q.getCorrectAnswers());
                    wrongQ.put("userAnswer", userAnswer);
                    wrongQ.put("explanation", q.getExplanation());
                    wrongQuestions.add(wrongQ);
                }
            }
        }
        
        result.put("correct", correct);
        result.put("wrong", wrong);
        result.put("unanswered", unanswered);
        result.put("wrongQuestions", wrongQuestions);
        
        // 计算时长（从created_at到submitted_at）
        if (exam.getCreatedAt() != null && exam.getSubmittedAt() != null) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long start = sdf.parse(exam.getCreatedAt()).getTime();
                long end = sdf.parse(exam.getSubmittedAt()).getTime();
                long duration = (end - start) / 1000 / 60; // 分钟
                result.put("duration", duration + "分钟");
            } catch (Exception e) {
                result.put("duration", "未知");
        }
        } else {
            result.put("duration", "未知");
        }
        
        // 计算正确率
        int total = correct + wrong;
        double accuracy = total > 0 ? (double) correct / total * 100 : 0;
        result.put("accuracy", Math.round(accuracy));
        
        return result;
    }
    
    private void handleGetExamResult(int id, int userId, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        Exam exam = ExamDAO.getExamResult(id, userId);
        if (exam != null) {
            // 转换为前端期望的格式
            Map<String, Object> result = convertExamToResult(exam);
            out.print(JsonUtil.writeValueAsString(result));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Exam result not found\"}");
        }
    }
    
    private void handleGetExamHistory(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
        int limit = Integer.parseInt(request.getParameter("limit") != null ? request.getParameter("limit") : 
                                     (request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "20"));
        
        List<Exam> exams = ExamDAO.getExamHistory(userId, page, limit);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> examList = new ArrayList<>();
        for (Exam exam : exams) {
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("id", exam.getId());
            examMap.put("title", "模拟考试");
            examMap.put("passed", exam.isPassed());
            examMap.put("score", exam.getScore() != null ? exam.getScore() : 0);
            examMap.put("total", exam.getQuestions() != null ? exam.getQuestions().size() : 0);
            
            // 计算答对题目数（从score字段直接获取，避免查询题目详情）
            // score字段已经存储了答对的题目数
            int correct = exam.getScore() != null ? exam.getScore() : 0;
            examMap.put("correct", correct);
            
            // 格式化日期
            if (exam.getSubmittedAt() != null) {
                examMap.put("date", exam.getSubmittedAt());
            } else if (exam.getCreatedAt() != null) {
                examMap.put("date", exam.getCreatedAt());
            } else {
                examMap.put("date", "");
            }
            
            // 计算用时（分钟）
            String duration = "未知";
            if (exam.getCreatedAt() != null && exam.getSubmittedAt() != null) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date start = sdf.parse(exam.getCreatedAt());
                    java.util.Date end = sdf.parse(exam.getSubmittedAt());
                    long diff = end.getTime() - start.getTime();
                    long minutes = diff / (1000 * 60);
                    duration = minutes + "分钟";
                } catch (Exception e) {
                    // 解析失败，使用默认值
                }
            }
            examMap.put("duration", duration);
            
            examList.add(examMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", examList);
        out.print(JsonUtil.writeValueAsString(result));
    }
}

