package com.exam.servlet;

import com.exam.dao.QuestionDAO;
import com.exam.model.Question;
import com.exam.util.AuthUtil;
import com.exam.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/questions/*")
@MultipartConfig(maxFileSize = 10485760) // 10MB
public class QuestionServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(QuestionServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetQuestions(request, response, out);
            } else if (pathInfo.startsWith("/random/")) {
                handleGetRandomQuestions(request, response, out);
            } else if (pathInfo.startsWith("/sequential/")) {
                handleGetSequentialQuestions(request, response, out);
            } else {
                // /{id}/
                String idStr = pathInfo.replaceAll("/", "");
                try {
                    int id = Integer.parseInt(idStr);
                    handleGetQuestion(id, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid question ID\"}");
                }
            }
        } catch (Exception e) {
            logger.error("Error handling questions GET request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
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
                handleCreateQuestion(request, response, out);
            } else if (pathInfo.equals("/import/")) {
                handleImportQuestions(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            out.print("{\"error\": \"" + errorMsg.replace("\"", "\\\"") + "\"}");
            e.printStackTrace(); // 打印堆栈跟踪到服务器日志
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        
        try {
            if (!AuthUtil.isAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                String idStr = pathInfo.replaceAll("/", "");
                try {
                    int id = Integer.parseInt(idStr);
                    handleUpdateQuestion(id, request, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid question ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing question ID\"}");
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
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                String idStr = pathInfo.replaceAll("/", "");
                try {
                    int id = Integer.parseInt(idStr);
                    QuestionDAO.deleteQuestion(id);
                    out.print("{\"message\": \"Question deleted successfully\"}");
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid question ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing question ID\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleGetQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        String search = request.getParameter("search");
        String type = request.getParameter("type");
        String difficulty = request.getParameter("difficulty");
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
        int pageSize = Integer.parseInt(request.getParameter("page_size") != null ? request.getParameter("page_size") : 
                                       (request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "20"));
        
        List<Question> questions = QuestionDAO.getQuestions(search, type, difficulty, page, pageSize);
        int totalCount = QuestionDAO.getQuestionsCount(search, type, difficulty);
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", questions);
        result.put("count", totalCount);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetQuestion(int id, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        Question question = QuestionDAO.getQuestionById(id);
        if (question != null) {
            out.print(JsonUtil.writeValueAsString(question));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Question not found\"}");
        }
    }
    
    private void handleCreateQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Question question = JsonUtil.readValue(reader, Question.class);
        
        Question created = QuestionDAO.createQuestion(question);
        if (created != null) {
            out.print(JsonUtil.writeValueAsString(created));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Failed to create question\"}");
        }
    }
    
    private void handleUpdateQuestion(int id, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        Question question = JsonUtil.readValue(reader, Question.class);
        
        QuestionDAO.updateQuestion(id, question);
        Question updated = QuestionDAO.getQuestionById(id);
        if (updated != null) {
            out.print(JsonUtil.writeValueAsString(updated));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Question not found\"}");
        }
    }
    
    private void handleGetRandomQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        int count = Integer.parseInt(request.getParameter("count") != null ? request.getParameter("count") : "10");
        String type = request.getParameter("type"); // 支持按类型筛选
        
        List<Question> questions = QuestionDAO.getRandomQuestions(count, type);
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", questions);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetSequentialQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        int offset = Integer.parseInt(request.getParameter("offset") != null ? request.getParameter("offset") : "0");
        int limit = Integer.parseInt(request.getParameter("limit") != null ? request.getParameter("limit") : "10");
        String type = request.getParameter("type"); // 支持按类型筛选
        
        List<Question> questions = QuestionDAO.getSequentialQuestions(offset, limit, type);
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", questions);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleImportQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException, ServletException {
        try {
            // 检查管理员权限
            if (!AuthUtil.isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Forbidden\"}");
                return;
            }
            
            // 检查请求是否为multipart
            String contentType = request.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Request must be multipart/form-data\"}");
                return;
            }
            
            // 获取上传的文件
            Part filePart = null;
            try {
                filePart = request.getPart("file");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Failed to parse multipart request: " + e.getMessage() + "\"}");
                return;
            }
            
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"No file uploaded\"}");
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || fileName.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid file name\"}");
                return;
            }
            
            // 根据文件扩展名判断格式
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"File must have an extension\"}");
                return;
            }
            
            String fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
            List<Question> questions = new ArrayList<>();
            
            try (InputStream fileContent = filePart.getInputStream()) {
                switch (fileExtension) {
                    case "json":
                        questions = parseJsonFile(fileContent);
                        break;
                    case "csv":
                        questions = parseCsvFile(fileContent);
                        break;
                    case "xlsx":
                    case "xls":
                        questions = parseExcelFile(fileContent, fileExtension);
                        break;
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Unsupported file format. Supported formats: JSON, CSV, Excel\"}");
                        return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Failed to parse file: " + e.getMessage() + "\"}");
                return;
            }
            
            if (questions.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"No valid questions found in file\"}");
                return;
            }
            
            // 批量插入到数据库
            int successCount = 0;
            try {
                successCount = QuestionDAO.batchCreateQuestions(questions);
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database error: " + e.getMessage() + "\"}");
                return;
            }
            
            int failedCount = questions.size() - successCount;
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> stats = new HashMap<>();
            stats.put("success", successCount);
            stats.put("failed", failedCount);
            result.put("stats", stats);
            result.put("message", "导入完成");
            
            out.print(JsonUtil.writeValueAsString(result));
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            out.print("{\"error\": \"Import failed: " + errorMsg.replace("\"", "\\\"") + "\"}");
        }
    }
    
    private List<Question> parseJsonFile(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // 读取所有内容
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            
            String jsonContent = content.toString().trim();
            if (jsonContent.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 先解析为 Map 列表，以便手动处理 correctAnswer 字段
            List<Question> questions = new ArrayList<>();
            try {
                // 尝试解析为数组
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> questionMaps = mapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> qMap : questionMaps) {
                    Question q = convertMapToQuestion(qMap);
                    if (q != null) {
                        questions.add(q);
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，尝试解析为包含questions字段的对象
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> wrapper = mapper.readValue(jsonContent, Map.class);
                    if (wrapper.containsKey("questions")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) wrapper.get("questions");
                        for (Map<String, Object> qMap : questionMaps) {
                            Question q = convertMapToQuestion(qMap);
                            if (q != null) {
                                questions.add(q);
                            }
                        }
                    } else {
                        throw new IOException("Invalid JSON format: " + e.getMessage());
                    }
                } catch (Exception e2) {
                    throw new IOException("Failed to parse JSON: " + e.getMessage());
                }
            }
            
            // 验证和规范化数据
            List<Question> validQuestions = new ArrayList<>();
            for (Question q : questions) {
                if (isValidQuestion(q)) {
                    normalizeQuestion(q);
                    validQuestions.add(q);
                }
            }
            return validQuestions;
        }
    }
    
    /**
     * 将 Map 转换为 Question 对象，特殊处理 correctAnswer 字段
     */
    @SuppressWarnings("unchecked")
    private Question convertMapToQuestion(Map<String, Object> qMap) {
        try {
            Question q = new Question();
            
            // 设置基本字段
            if (qMap.containsKey("type")) {
                q.setType(String.valueOf(qMap.get("type")));
            }
            if (qMap.containsKey("difficulty")) {
                q.setDifficulty(String.valueOf(qMap.get("difficulty")));
            }
            if (qMap.containsKey("question")) {
                q.setQuestion(String.valueOf(qMap.get("question")));
            }
            if (qMap.containsKey("options")) {
                Object optionsObj = qMap.get("options");
                if (optionsObj instanceof List) {
                    List<String> options = new ArrayList<>();
                    for (Object opt : (List<?>) optionsObj) {
                        options.add(String.valueOf(opt));
                    }
                    q.setOptions(options);
                }
            }
            if (qMap.containsKey("explanation")) {
                q.setExplanation(String.valueOf(qMap.get("explanation")));
            }
            
            // 特殊处理 correctAnswer 字段
            if (qMap.containsKey("correctAnswer")) {
                Object correctAnswerObj = qMap.get("correctAnswer");
                
                // 如果是数组，说明是多选题
                if (correctAnswerObj instanceof List) {
                    List<Integer> correctAnswers = new ArrayList<>();
                    for (Object ans : (List<?>) correctAnswerObj) {
                        if (ans instanceof Number) {
                            correctAnswers.add(((Number) ans).intValue());
                        } else {
                            try {
                                correctAnswers.add(Integer.parseInt(String.valueOf(ans)));
                            } catch (NumberFormatException e) {
                                // 忽略无效的答案
                            }
                        }
                    }
                    if (!correctAnswers.isEmpty()) {
                        q.setCorrectAnswers(correctAnswers);
                        q.setCorrectAnswer(correctAnswers.get(0)); // 向后兼容
                        // 如果类型不是 multiple，设置为 multiple
                        if (q.getType() == null || !q.getType().equals("multiple")) {
                            q.setType("multiple");
                        }
                    }
                } else if (correctAnswerObj instanceof Number) {
                    // 如果是单个数字，说明是单选题或判断题
                    int correctAnswer = ((Number) correctAnswerObj).intValue();
                    q.setCorrectAnswer(correctAnswer);
                } else {
                    // 尝试解析为整数
                    try {
                        int correctAnswer = Integer.parseInt(String.valueOf(correctAnswerObj));
                        q.setCorrectAnswer(correctAnswer);
                    } catch (NumberFormatException e) {
                        // 忽略无效的答案
                    }
                }
            }
            
            // 如果存在 correctAnswers 字段（备用）
            if (qMap.containsKey("correctAnswers")) {
                Object correctAnswersObj = qMap.get("correctAnswers");
                if (correctAnswersObj instanceof List) {
                    List<Integer> correctAnswers = new ArrayList<>();
                    for (Object ans : (List<?>) correctAnswersObj) {
                        if (ans instanceof Number) {
                            correctAnswers.add(((Number) ans).intValue());
                        } else {
                            try {
                                correctAnswers.add(Integer.parseInt(String.valueOf(ans)));
                            } catch (NumberFormatException e) {
                                // 忽略无效的答案
                            }
                        }
                    }
                    if (!correctAnswers.isEmpty()) {
                        q.setCorrectAnswers(correctAnswers);
                        q.setCorrectAnswer(correctAnswers.get(0)); // 向后兼容
                        q.setType("multiple");
                    }
                }
            }
            
            return q;
        } catch (Exception e) {
            logger.error("Failed to convert map to question: " + e.getMessage(), e);
            return null;
        }
    }
    
    private List<Question> parseCsvFile(InputStream inputStream) throws IOException {
        List<Question> questions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // 跳过表头
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 解析CSV行（简单实现，假设用逗号分隔，不处理引号内的逗号）
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    Question q = new Question();
                    q.setType(parts[0].trim().equalsIgnoreCase("判断题") ? "judge" : "single");
                    q.setDifficulty(parts[1].trim().toLowerCase());
                    q.setQuestion(parts[2].trim());
                    
                    List<String> options = new ArrayList<>();
                    options.add(parts[3].trim());
                    options.add(parts[4].trim());
                    if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                        options.add(parts[5].trim());
                    }
                    if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                        options.add(parts[6].trim());
                    }
                    q.setOptions(options);
                    
                    // 正确答案：支持单个答案（A）或多个答案（A,B或AB）
                    String answer = parts[7].trim().toUpperCase();
                    if (answer.contains(",") || answer.length() > 1) {
                        // 多选题：解析多个答案
                        q.setType("multiple");
                        List<Integer> correctAnswers = new ArrayList<>();
                        String[] answers = answer.contains(",") ? answer.split(",") : answer.split("");
                        for (String ans : answers) {
                            ans = ans.trim();
                            if (ans.equals("A")) correctAnswers.add(0);
                            else if (ans.equals("B")) correctAnswers.add(1);
                            else if (ans.equals("C")) correctAnswers.add(2);
                            else if (ans.equals("D")) correctAnswers.add(3);
                        }
                        if (!correctAnswers.isEmpty()) {
                            q.setCorrectAnswers(correctAnswers);
                            q.setCorrectAnswer(correctAnswers.get(0)); // 向后兼容
                        }
                    } else {
                        // 单选题或判断题
                        int correctAnswer = 0;
                        if (answer.equals("A")) correctAnswer = 0;
                        else if (answer.equals("B")) correctAnswer = 1;
                        else if (answer.equals("C")) correctAnswer = 2;
                        else if (answer.equals("D")) correctAnswer = 3;
                        else if (answer.equals("正确") || answer.equals("0")) correctAnswer = 0;
                        else if (answer.equals("错误") || answer.equals("1")) correctAnswer = 1;
                        q.setCorrectAnswer(correctAnswer);
                    }
                    
                    q.setExplanation(parts.length > 8 ? parts[8].trim() : "");
                    
                    if (isValidQuestion(q)) {
                        normalizeQuestion(q);
                        questions.add(q);
                    }
                }
            }
        }
        
        return questions;
    }
    
    private List<Question> parseExcelFile(InputStream inputStream, String fileExtension) throws IOException {
        List<Question> questions = new ArrayList<>();
        
        Workbook workbook;
        if (fileExtension.equals("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            workbook = new HSSFWorkbook(inputStream);
        }
        
        try {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue; // 跳过表头
                }
                
                if (row.getPhysicalNumberOfCells() < 8) {
                    continue;
                }
                
                Question q = new Question();
                
                // 题目类型
                String typeStr = getCellValueAsString(row.getCell(0));
                if (typeStr.contains("判断")) {
                    q.setType("judge");
                } else if (typeStr.contains("多选")) {
                    q.setType("multiple");
                } else {
                    q.setType("single");
                }
                
                // 难度
                q.setDifficulty(getCellValueAsString(row.getCell(1)).toLowerCase());
                
                // 题目内容
                q.setQuestion(getCellValueAsString(row.getCell(2)));
                
                // 选项
                List<String> options = new ArrayList<>();
                options.add(getCellValueAsString(row.getCell(3)));
                options.add(getCellValueAsString(row.getCell(4)));
                String optionC = getCellValueAsString(row.getCell(5));
                String optionD = getCellValueAsString(row.getCell(6));
                if (optionC != null && !optionC.isEmpty()) {
                    options.add(optionC);
                }
                if (optionD != null && !optionD.isEmpty()) {
                    options.add(optionD);
                }
                q.setOptions(options);
                
                // 正确答案：支持单个答案或多个答案
                String answerStr = getCellValueAsString(row.getCell(7)).toUpperCase();
                if (q.getType().equals("multiple") || answerStr.contains(",") || answerStr.length() > 1) {
                    // 多选题：解析多个答案
                    q.setType("multiple");
                    List<Integer> correctAnswers = new ArrayList<>();
                    String[] answers = answerStr.contains(",") ? answerStr.split(",") : answerStr.split("");
                    for (String ans : answers) {
                        ans = ans.trim();
                        if (ans.equals("A")) correctAnswers.add(0);
                        else if (ans.equals("B")) correctAnswers.add(1);
                        else if (ans.equals("C")) correctAnswers.add(2);
                        else if (ans.equals("D")) correctAnswers.add(3);
                    }
                    if (!correctAnswers.isEmpty()) {
                        q.setCorrectAnswers(correctAnswers);
                        q.setCorrectAnswer(correctAnswers.get(0)); // 向后兼容
                    }
                } else {
                    // 单选题或判断题
                    int correctAnswer = 0;
                    if (answerStr.equals("A")) correctAnswer = 0;
                    else if (answerStr.equals("B")) correctAnswer = 1;
                    else if (answerStr.equals("C")) correctAnswer = 2;
                    else if (answerStr.equals("D")) correctAnswer = 3;
                    else if (answerStr.equals("正确") || answerStr.equals("0")) correctAnswer = 0;
                    else if (answerStr.equals("错误") || answerStr.equals("1")) correctAnswer = 1;
                    q.setCorrectAnswer(correctAnswer);
    }
                
                // 答案解析
                if (row.getPhysicalNumberOfCells() > 8) {
                    q.setExplanation(getCellValueAsString(row.getCell(8)));
                } else {
                    q.setExplanation("");
                }
                
                if (isValidQuestion(q)) {
                    normalizeQuestion(q);
                    questions.add(q);
                }
            }
        } finally {
            workbook.close();
        }
        
        return questions;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // 处理数字，避免科学计数法
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // 对于公式单元格，尝试获取计算后的值
                    try {
                        DataFormatter formatter = new DataFormatter();
                        return formatter.formatCellValue(cell);
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            // 如果获取值失败，返回空字符串
            return "";
        }
    }
    
    private boolean isValidQuestion(Question q) {
        if (q == null || q.getType() == null || q.getQuestion() == null || q.getQuestion().trim().isEmpty() ||
            q.getOptions() == null || q.getOptions().isEmpty()) {
            return false;
        }
        
        // 验证题目类型
        if (!q.getType().equals("single") && !q.getType().equals("judge") && !q.getType().equals("multiple")) {
            return false;
        }
        
        // 验证正确答案
        if (q.getType().equals("multiple")) {
            // 多选题：验证correctAnswers
            if (q.getCorrectAnswers() == null || q.getCorrectAnswers().isEmpty()) {
                return false;
            }
            for (Integer answer : q.getCorrectAnswers()) {
                if (answer < 0 || answer >= q.getOptions().size()) {
                    return false;
                }
            }
        } else {
            // 单选题和判断题：验证correctAnswer
            if (q.getCorrectAnswer() < 0 || q.getCorrectAnswer() >= q.getOptions().size()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void normalizeQuestion(Question q) {
        // 规范化难度值
        String difficulty = q.getDifficulty();
        if (difficulty == null || difficulty.isEmpty()) {
            q.setDifficulty("easy");
        } else {
            String lower = difficulty.toLowerCase();
            if (lower.equals("简单") || lower.equals("easy")) {
                q.setDifficulty("easy");
            } else if (lower.equals("中等") || lower.equals("medium")) {
                q.setDifficulty("medium");
            } else if (lower.equals("困难") || lower.equals("hard")) {
                q.setDifficulty("hard");
            } else {
                q.setDifficulty("easy");
            }
        }
        
        // 确保explanation不为null
        if (q.getExplanation() == null) {
            q.setExplanation("");
        }
    }
    
}

