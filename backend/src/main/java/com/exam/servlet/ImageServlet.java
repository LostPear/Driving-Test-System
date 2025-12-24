package com.exam.servlet;

import com.exam.util.AuthUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@WebServlet("/api/images/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 10 * 1024 * 1024, // 10MB
    maxRequestSize = 10 * 1024 * 1024 // 10MB
)
public class ImageServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ImageServlet.class);
    private static final String UPLOAD_DIR = "uploads/images";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    
    @Override
    public void init() throws ServletException {
        super.init();
        // 创建上传目录
        try {
            Path uploadPath = Paths.get(getServletContext().getRealPath("/"), UPLOAD_DIR);
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            logger.error("Failed to create upload directory", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // 移除开头的斜杠
        String imageName = pathInfo.substring(1);
        
        // 安全检查：防止路径遍历攻击
        if (imageName.contains("..") || imageName.contains("/") || imageName.contains("\\")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            Path imagePath = Paths.get(getServletContext().getRealPath("/"), UPLOAD_DIR, imageName);
            
            if (!Files.exists(imagePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置缓存头，优化加载速度
            response.setHeader("Cache-Control", "public, max-age=31536000"); // 1年缓存
            long expiresTime = System.currentTimeMillis() + 31536000000L; // 1年后
            response.setDateHeader("Expires", expiresTime);
            response.setHeader("ETag", "\"" + imageName + "\"");
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(imageName);
            response.setContentType(contentType);
            
            // 读取并输出图片
            try (InputStream is = Files.newInputStream(imagePath);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            logger.error("Error serving image: " + imageName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 检查管理员权限
            if (!AuthUtil.isAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Forbidden\"}");
                return;
            }
            
            // 获取上传的文件
            Part filePart = request.getPart("image");
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"No image file uploaded\"}");
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || fileName.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid file name\"}");
                return;
            }
            
            // 检查文件扩展名
            String extension = getFileExtension(fileName).toLowerCase();
            boolean allowed = false;
            for (String ext : ALLOWED_EXTENSIONS) {
                if (ext.equals(extension)) {
                    allowed = true;
                    break;
                }
            }
            
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"File type not allowed. Allowed types: jpg, jpeg, png, gif, webp\"}");
                return;
            }
            
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path uploadPath = Paths.get(getServletContext().getRealPath("/"), UPLOAD_DIR);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(uniqueFileName);
            
            // 保存文件
            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 压缩图片（如果需要）
            compressImage(filePath);
            
            // 返回图片URL
            String imageUrl = "/api/images/" + uniqueFileName;
            out.print("{\"url\": \"" + imageUrl + "\", \"filename\": \"" + uniqueFileName + "\"}");
            
        } catch (Exception e) {
            logger.error("Error uploading image", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to upload image: " + e.getMessage() + "\"}");
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
    
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
    
    private void compressImage(Path imagePath) {
        // 图片压缩逻辑占位，可后续扩展
    }
}

