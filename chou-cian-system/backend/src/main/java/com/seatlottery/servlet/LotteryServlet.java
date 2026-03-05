package com.seatlottery.servlet;

import com.seatlottery.service.LotteryService;
import com.seatlottery.util.ResponseUtil;
import com.seatlottery.util.UrlUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LotteryServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LotteryServlet.class.getName());
    private LotteryService lotteryService;

    @Override
    public void init() throws ServletException {
        this.lotteryService = new LotteryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理抽籤GET請求，URI: " + uri);

        try {
            if (uri.endsWith("/result")) {
                // GET /api/activities/{activityId}/result - 獲取抽籤結果
                int activityId = UrlUtil.extractActivityId(uri);
                logger.info("獲取抽籤結果，活動ID: " + activityId);

                Map<String, Object> result = lotteryService.getLotteryResult(activityId);

                if (result != null) {
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到抽籤結果");
                }
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析活動ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的活動ID: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理抽籤POST請求，URI: " + uri);

        try {
            if (uri.endsWith("/lottery")) {
                // POST /api/activities/{activityId}/lottery - 執行抽籤
                int activityId = UrlUtil.extractActivityId(uri);
                logger.info("執行抽籤，活動ID: " + activityId);

                boolean success = lotteryService.runLottery(activityId);

                if (success) {
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, "抽籤已成功執行");
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "抽籤執行失敗");
                }
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析活動ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的活動ID: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "請求參數錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}