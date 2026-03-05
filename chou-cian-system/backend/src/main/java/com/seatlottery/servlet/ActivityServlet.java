package com.seatlottery.servlet;

import com.seatlottery.dto.ActivityCreateDto;  // 添加 DTO import
import com.seatlottery.model.Activity;
import com.seatlottery.service.ActivityService;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ActivityServlet.class.getName());
    private ActivityService activityService;

    @Override
    public void init() throws ServletException {
        this.activityService = new ActivityService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理活動請求，URI: " + uri);

        try {
            if (uri.matches(".*/activities$") || uri.matches(".*/activities/$")) {
                // GET /api/activities - 獲取所有活動
                List<Activity> activities = activityService.getAllActivities();
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, activities);
            } else {
                // GET /api/activities/{id} - 獲取單個活動
                int id = UrlUtil.extractActivityId(uri);
                logger.info("獲取活動詳情，ID: " + id);

                Activity activity = activityService.getActivityById(id);

                if (activity != null) {
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, activity);
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
                }
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
        try {
            logger.info("創建新活動");
            // POST /api/activities - 創建新活動
            // 修改：使用 ActivityCreateDto 接收前端數據
            ActivityCreateDto activityDto = JsonUtil.parseRequestBody(request, ActivityCreateDto.class);
            Activity createdActivity = activityService.createActivity(activityDto);  // 使用 DTO 版本的方法
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_CREATED, createdActivity);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "請求數據無效", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "請求數據無效: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("更新活動，URI: " + uri);

        try {
            // PUT /api/activities/{id} - 更新活動
            int id = UrlUtil.extractActivityId(uri);
            logger.info("更新活動，ID: " + id);

            Activity activity = JsonUtil.parseRequestBody(request, Activity.class);
            activity.setId(id);

            Activity updatedActivity = activityService.updateActivity(activity);
            if (updatedActivity != null) {
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, updatedActivity);
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析活動ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的活動ID: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "請求數據無效", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "請求數據無效: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("刪除活動，URI: " + uri);

        try {
            // DELETE /api/activities/{id} - 刪除活動
            int id = UrlUtil.extractActivityId(uri);
            logger.info("刪除活動，ID: " + id);

            boolean deleted = activityService.deleteActivity(id);

            if (deleted) {
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, "活動已成功刪除");
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析活動ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的活動ID: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        }
    }
}