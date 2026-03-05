package com.seatlottery.servlet;

import com.seatlottery.model.Participant;
import com.seatlottery.model.Selection;
import com.seatlottery.service.ParticipantService;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParticipantServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ParticipantServlet.class.getName());
    private ParticipantService participantService;

    @Override
    public void init() throws ServletException {
        this.participantService = new ParticipantService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理參與者請求，URI: " + uri);

        try {
            if (uri.matches(".*/participants$")) {
                // GET /api/activities/{activityId}/participants - 獲取所有參與者
                int activityId = UrlUtil.extractActivityId(uri);
                logger.info("獲取活動參與者，活動ID: " + activityId);

                List<Participant> participants = participantService.getParticipants(activityId);
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, participants);
            } else if (uri.matches(".*/participants/.*/selection$")) {
                // GET /api/activities/{activityId}/participants/{participantId}/selection - 獲取參與者選座
                int activityId = UrlUtil.extractActivityId(uri);
                int participantId = UrlUtil.extractParticipantId(uri);
                logger.info("獲取參與者選座，活動ID: " + activityId + "，參與者ID: " + participantId);

                Selection selection = participantService.getUserSelection(activityId, participantId);

                if (selection != null) {
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, selection);
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到選座記錄");
                }
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的ID格式: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理參與者POST請求，URI: " + uri);

        try {
            if (uri.matches(".*/participants/join$")) {
                // POST /api/activities/{activityId}/participants/join - 參加活動
                int activityId = UrlUtil.extractActivityId(uri);
                logger.info("參與者加入活動，活動ID: " + activityId);

                Participant participant = JsonUtil.parseRequestBody(request, Participant.class);
                participant.setActivityId(activityId);

                Participant createdParticipant = participantService.joinActivity(participant);
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_CREATED, createdParticipant);
            } else if (uri.matches(".*/seats/.*/select$")) {
                // POST /api/activities/{activityId}/seats/{seatId}/select - 選擇座位
                int activityId = UrlUtil.extractActivityId(uri);
                String seatId = UrlUtil.extractSeatId(uri);
                logger.info("選擇座位，活動ID: " + activityId + "，座位ID: " + seatId);

                Map<String, Object> requestBody = JsonUtil.parseRequestBody(request, Map.class);
                int participantId = ((Number) requestBody.get("userId")).intValue();

                Selection selection = participantService.selectSeat(activityId, participantId, seatId);
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, selection);
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的ID格式: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "請求參數錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        logger.info("處理參與者DELETE請求，URI: " + uri);

        try {
            if (uri.matches(".*/participants/.*/selection$")) {
                // DELETE /api/activities/{activityId}/participants/{participantId}/selection - 取消選座
                int activityId = UrlUtil.extractActivityId(uri);
                int participantId = UrlUtil.extractParticipantId(uri);
                logger.info("取消選座，活動ID: " + activityId + "，參與者ID: " + participantId);

                boolean success = participantService.cancelSeatSelection(activityId, participantId);
                if (success) {
                    ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, "已成功取消選座");
                } else {
                    ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到選座記錄");
                }
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "無效的請求路徑");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的ID格式: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        }
    }
}