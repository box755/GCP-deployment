package com.seatlottery.filter;

import com.seatlottery.model.Admin;
import com.seatlottery.service.AuthService;
import com.seatlottery.util.ResponseUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthFilter implements Filter {
    private AuthService authService;
    private List<String> publicUrls;           // 完全公開的 URL
    private List<String> userUrls;             // 用戶端可以訪問的 URL
    private List<String> adminOnlyUrls;        // 只有管理員可以訪問的 URL

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        authService = new AuthService();

        // 🌐 完全公開的 URL（不需要任何認證）
        publicUrls = Arrays.asList(
                "/api/auth/login",                                          // 管理員登入
                "/api/auth/verify"                                          // Token 驗證
        );

        // 👤 用戶端可以訪問的 URL（用戶功能，不需要管理員權限）
        userUrls = Arrays.asList(
                "/api/activities/\\d+$",                                    // 獲取活動詳情
                "/api/activities/\\d+/seats$",                             // 獲取座位列表
                "/api/activities/\\d+/selections$",                        // 獲取選座記錄
                "/api/activities/\\d+/participants/join$",                 // 參加活動 (POST)
                "/api/activities/\\d+/participants/\\d+/selection$",       // 用戶選座記錄 (GET/DELETE)
                "/api/activities/\\d+/seats/[^/]+/select$"                 // 選擇座位 (POST)
        );

        // 🔐 管理員專用 URL（需要管理員權限）
        adminOnlyUrls = Arrays.asList(
                "/api/activities$",                                         // 獲取所有活動 (GET) + 創建活動 (POST)
                "/api/activities/\\d+/participants$",                      // 獲取參與者列表 (GET)
                "/api/activities/\\d+/lottery$",                           // 執行抽籤 (POST)
                "/api/activities/\\d+/lottery/rerun$",                     // 重新抽籤 (POST)
                "/api/activities/\\d+/lottery/results$",                   // 獲取抽籤結果 (GET)
                "/api/activities/\\d+/result$",                            // 獲取抽籤結果-舊版 (GET)
                "/api/activities/\\d+/end-selection$",                     // 提前結束選位 (PUT)
                "/api/activities/\\d+/resume-selection$",                  // 恢復選位 (PUT)
                "/api/activities/\\d+$"                                    // 更新活動 (PUT) + 刪除活動 (DELETE)
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod();

        System.out.println("🔍 AuthFilter 檢查: " + method + " " + path);

        // 1. 檢查是否是完全公開的 URL
        if (isPublicUrl(path)) {
            System.out.println("🌐 公開 URL，跳過認證: " + path);
            chain.doFilter(request, response);
            return;
        }

        // 2. 檢查是否是用戶端 URL
        if (isUserUrl(path)) {
            System.out.println("👤 用戶端 URL，允許訪問: " + path);
            chain.doFilter(request, response);
            return;
        }

        // 3. 檢查是否是管理員專用 URL
        if (isAdminOnlyUrl(path)) {
            System.out.println("🔐 管理員專用 URL，需要認證: " + path);

            if (!validateAdminToken(httpRequest, httpResponse)) {
                return; // 認證失敗，已返回錯誤響應
            }

            chain.doFilter(request, response);
            return;
        }

        // 4. 對於其他未明確分類的 URL，默認需要管理員權限
        System.out.println("❓ 其他 URL，默認需要管理員認證: " + path);

        if (!validateAdminToken(httpRequest, httpResponse)) {
            return; // 認證失敗，已返回錯誤響應
        }

        chain.doFilter(request, response);
    }

    /**
     * 驗證管理員 Token
     */
    private boolean validateAdminToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ 缺少認證 token");
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "未授權：管理員功能需要登入");
            return false;
        }

        String token = authHeader.substring(7);

        // 驗證 token
        Admin admin = authService.validateToken(token);
        if (admin == null) {
            System.out.println("❌ 無效的認證 token");
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "未授權：無效的認證信息");
            return false;
        }

        System.out.println("✅ 管理員認證成功: " + admin.getUsername());
        // 將管理員信息添加到請求屬性中
        request.setAttribute("admin", admin);
        return true;
    }

    private boolean isPublicUrl(String url) {
        for (String pattern : publicUrls) {
            if (url.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserUrl(String url) {
        for (String pattern : userUrls) {
            if (url.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminOnlyUrl(String url) {
        for (String pattern : adminOnlyUrls) {
            if (url.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // 清理代碼
    }
}