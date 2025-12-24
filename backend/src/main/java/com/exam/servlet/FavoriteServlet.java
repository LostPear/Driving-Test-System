package com.exam.servlet;

import com.exam.dao.FavoriteDAO;
import com.exam.dao.QuestionDAO;
import com.exam.model.Question;
import com.exam.util.AuthUtil;
import com.exam.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.Map;

@WebServlet("/api/favorites/*")
public class FavoriteServlet extends HttpServlet {
    
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
                // 获取收藏列表
                handleGetFavorites(userId, request, response, out);
            } else if (pathInfo.startsWith("/check/")) {
                // 检查题目是否已收藏
                String questionIdStr = pathInfo.replace("/check/", "").replace("/", "");
                try {
                    int questionId = Integer.parseInt(questionIdStr);
                    handleCheckFavorite(userId, questionId, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid question ID\"}");
                }
            } else if (pathInfo.startsWith("/ids/")) {
                // 获取收藏的题目ID列表
                handleGetFavoriteIds(userId, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
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
            
            int userId = AuthUtil.getUserId(request);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // 添加收藏
                handleAddFavorite(userId, request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Not found\"}");
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
            
            int userId = AuthUtil.getUserId(request);
            
            if (pathInfo != null && !pathInfo.equals("/")) {
                // 删除收藏
                String questionIdStr = pathInfo.replace("/", "");
                try {
                    int questionId = Integer.parseInt(questionIdStr);
                    handleRemoveFavorite(userId, questionId, response, out);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid question ID\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Question ID required\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleGetFavorites(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
        int pageSize = Integer.parseInt(request.getParameter("page_size") != null ? request.getParameter("page_size") :
                                       (request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "20"));
        
        List<Question> questions = FavoriteDAO.getFavorites(userId, page, pageSize);
        int totalCount = FavoriteDAO.getFavoritesCount(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("results", questions);
        result.put("count", totalCount);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleAddFavorite(int userId, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        String questionIdStr = request.getParameter("question_id");
        
        // 如果参数为空，尝试从请求体读取JSON
        if (questionIdStr == null || questionIdStr.isEmpty()) {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String body = sb.toString();
                if (body != null && !body.isEmpty()) {
                    Map<String, Object> bodyMap = JsonUtil.readValue(body, new TypeReference<Map<String, Object>>() {});
                    if (bodyMap != null && bodyMap.containsKey("question_id")) {
                        questionIdStr = String.valueOf(bodyMap.get("question_id"));
                    }
                }
            } catch (Exception e) {
                // 忽略JSON解析错误，继续使用参数
            }
        }
        
        if (questionIdStr == null || questionIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"question_id parameter required\"}");
            return;
        }
        
        try {
            int questionId = Integer.parseInt(questionIdStr);
            boolean success = FavoriteDAO.addFavorite(userId, questionId);
            
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "收藏成功");
                out.print(JsonUtil.writeValueAsString(result));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to add favorite\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid question_id\"}");
        }
    }
    
    private void handleRemoveFavorite(int userId, int questionId, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        boolean success = FavoriteDAO.removeFavorite(userId, questionId);
        
        if (success) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "取消收藏成功");
            out.print(JsonUtil.writeValueAsString(result));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to remove favorite\"}");
        }
    }
    
    private void handleCheckFavorite(int userId, int questionId, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        boolean isFavorite = FavoriteDAO.isFavorite(userId, questionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        out.print(JsonUtil.writeValueAsString(result));
    }
    
    private void handleGetFavoriteIds(int userId, HttpServletResponse response, PrintWriter out)
            throws SQLException, IOException {
        List<Integer> questionIds = FavoriteDAO.getFavoriteQuestionIds(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("questionIds", questionIds);
        out.print(JsonUtil.writeValueAsString(result));
    }
}

