package com.seatlottery.servlet;

import com.seatlottery.service.AuthService;
import com.seatlottery.util.JsonUtil;
import com.seatlottery.util.ResponseUtil;
import com.seatlottery.util.UrlUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理認證POST請求，URI: " + uri);

        try {
            String lastSegment = UrlUtil.extractLastPathSegment(uri);
            if ("login".equals(lastSegment)) {
                // POST /api/auth/login - 管理員登入
                Map<String, String> credentials = JsonUtil.parseRequestBody(request, Map.class);
                String username = credentials.get("username");
                String password = credentials.get("password");

                logger.info("嘗試登入，用戶名: " + username);

                if (username == null || password == null) {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "缺少用戶名或密碼");
                    return;
                }

                String token = authService.login(username, password);

                if (token != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("token", token);
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
                    logger.info("用戶登入成功: " + username);
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "用戶名或密碼錯誤");
                    logger.warning("用戶登入失敗: " + username);
                }
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "請求處理錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "請求處理錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理認證GET請求，URI: " + uri);

        String lastSegment = UrlUtil.extractLastPathSegment(uri);
        if ("verify".equals(lastSegment)) {
            // GET /api/auth/verify - 驗證 token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未授權：缺少有效的認證信息");
                return;
            }

            String token = authHeader.substring(7); // 去掉 "Bearer " 前綴
            boolean isValid = authService.validateToken(token) != null;

            logger.info("驗證Token: " + (isValid ? "有效" : "無效"));

            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
        }
    }
}