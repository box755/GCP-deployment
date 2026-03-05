package com.seatlottery.servlet;

import com.seatlottery.dto.ActivityCreateDto;
import com.seatlottery.dto.ParticipantCreateDto;
import com.seatlottery.dto.SeatLayoutDto;
import com.seatlottery.model.*;
import com.seatlottery.service.*;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/*")
public class ApiController extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ApiController.class.getName());

    private ActivityService activityService;
    private SeatService seatService;
    private ParticipantService participantService;
    private LotteryService lotteryService;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.activityService = new ActivityService();
        this.seatService = new SeatService();
        this.participantService = new ParticipantService();
        this.lotteryService = new LotteryService();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        logger.info("API Controller 處理 " + method + " 請求: " + uri);

        try {
            // 路由分發
            if (uri.matches(".*/auth/verify$")) {
                handleAuthVerify(request, response);
            } else if (uri.matches(".*/activities$")) {
                handleGetAllActivities(request, response);
            } else if (uri.matches(".*/activities/\\d+$")) {
                handleGetActivity(request, response);
            } else if (uri.matches(".*/activities/\\d+/seats$")) {
                handleGetSeats(request, response);
            } else if (uri.matches(".*/activities/\\d+/selections$")) {
                handleGetSelections(request, response);
            } else if (uri.matches(".*/activities/\\d+/participants$")) {
                handleGetParticipants(request, response);
            } else if (uri.matches(".*/activities/\\d+/participants/\\d+/selection$")) {
                handleGetUserSelection(request, response);
            } else if (uri.matches(".*/activities/\\d+/result$")) {
                handleGetLotteryResult(request, response);
            } else if (uri.matches(".*/activities/\\d+/lottery/results$")) {
                handleGetLotteryResults(request, response);
            }else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到的路徑: " + uri);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "處理請求時發生錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "伺服器錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        logger.info("API Controller 處理 " + method + " 請求: " + uri);

        try {
            if (uri.matches(".*/auth/login$")) {
                handleAuthLogin(request, response);
            } else if (uri.matches(".*/activities$")) {
                handleCreateActivity(request, response);
            } else if (uri.matches(".*/activities/\\d+/participants/join$")) {
                handleJoinActivity(request, response);
            } else if (uri.matches(".*/activities/\\d+/seats/[^/]+/select$")) {
                handleSelectSeat(request, response);
            } else if (uri.matches(".*/activities/\\d+/lottery$")) {
                handleRunLottery(request, response);
            }else if (uri.matches(".*/activities/\\d+/lottery/rerun$")) {
                handleRerunLottery(request, response);
            }
            else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到的路徑: " + uri);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "處理請求時發生錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "伺服器錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        logger.info("API Controller 處理 " + method + " 請求: " + uri);

        try {
            if (uri.matches(".*/activities/\\d+$")) {
                handleUpdateActivity(request, response);
            }
            else if (uri.matches(".*/activities/\\d+/end-selection$")) {
                handleEndSelectionEarly(request, response);
            }
            else if (uri.matches(".*/activities/\\d+/resume-selection$")) {
                handleResumeSelection(request, response);
            }
            else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到的路徑: " + uri);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "處理請求時發生錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "伺服器錯誤: " + e.getMessage());
        }
    }



    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        logger.info("API Controller 處理 " + method + " 請求: " + uri);

        try {
            if (uri.matches(".*/activities/\\d+$")) {
                handleDeleteActivity(request, response);
            } else if (uri.matches(".*/activities/\\d+/participants/\\d+/selection$")) {
                handleCancelSeatSelection(request, response);
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到的路徑: " + uri);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "處理請求時發生錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "伺服器錯誤: " + e.getMessage());
        }
    }

    // ======== 認證相關處理方法 ========

    private void handleAuthLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        logger.info("處理登入請求");
        Map<String, String> credentials = JsonUtil.parseRequestBody(request, Map.class);
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "缺少用戶名或密碼");
            return;
        }

        String token = authService.login(username, password);

        if (token != null) {
            Map<String, Object> result = Map.of("token", token);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
            logger.info("用戶登入成功: " + username);
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "用戶名或密碼錯誤");
            logger.warning("用戶登入失敗: " + username);
        }
    }

    private void handleAuthVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("處理Token驗證請求");
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未授權：缺少有效的認證信息");
            return;
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token) != null;

        Map<String, Object> result = Map.of("valid", isValid);
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
        logger.info("Token驗證結果: " + (isValid ? "有效" : "無效"));
    }

    // ======== 活動相關處理方法 ========

    private void handleGetAllActivities(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        logger.info("獲取所有活動");
        List<Activity> activities = activityService.getAllActivities();
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, activities);
        logger.info("返回 " + activities.size() + " 個活動");
    }

    private void handleGetActivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取活動詳情，ID: " + activityId);

        Activity activity = activityService.getActivityById(activityId);

        if (activity != null) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, activity);
            logger.info("成功返回活動詳情: " + activity.getName());
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
            logger.warning("未找到活動，ID: " + activityId);
        }
    }

    // 添加處理方法
    private void handleResumeSelection(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("恢復選位請求，活動ID: " + activityId);

        try {
            Activity updatedActivity = activityService.resumeSelection(activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, updatedActivity);
            logger.info("成功恢復選位，活動ID: " + activityId);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "恢復選位失敗", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "恢復選位失敗: " + e.getMessage());
        }
    }

    private void handleCreateActivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        logger.info("創建新活動");

        try {
            // 使用 DTO 接收前端數據
            ActivityCreateDto activityDto = JsonUtil.parseRequestBody(request, ActivityCreateDto.class);

            // 添加調試信息
            logger.info("解析的活動信息: ");
            logger.info("名稱: " + activityDto.getName());
            logger.info("行數: " + activityDto.getRows() + ", 列數: " + activityDto.getCols());
            if (activityDto.getSeatLayout() != null) {
                logger.info("座位佈局數量: " + activityDto.getSeatLayout().size());
                // 打印前幾個座位的信息
                for (int i = 0; i < Math.min(3, activityDto.getSeatLayout().size()); i++) {
                    SeatLayoutDto seat = activityDto.getSeatLayout().get(i);
                    logger.info("座位 " + i + ": positionId=" + seat.getPositionId() +
                            ", row=" + seat.getRow() + ", col=" + seat.getCol() +
                            ", label=" + seat.getLabel());
                }
            }

            // 使用 DTO 創建活動
            Activity createdActivity = activityService.createActivity(activityDto);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_CREATED, createdActivity);
            logger.info("成功創建活動: " + createdActivity.getName() + "，ID: " + createdActivity.getId());
        } catch (Exception e) {
            logger.severe("創建活動時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void handleUpdateActivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("更新活動，ID: " + activityId);

        Activity activity = JsonUtil.parseRequestBody(request, Activity.class);
        activity.setId(activityId);

        Activity updatedActivity = activityService.updateActivity(activity);
        if (updatedActivity != null) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, updatedActivity);
            logger.info("成功更新活動: " + updatedActivity.getName());
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
            logger.warning("更新失敗，未找到活動，ID: " + activityId);
        }
    }

    private void handleDeleteActivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("刪除活動，ID: " + activityId);

        boolean deleted = activityService.deleteActivity(activityId);

        if (deleted) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, "活動已成功刪除");
            logger.info("成功刪除活動，ID: " + activityId);
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "找不到指定活動");
            logger.warning("刪除失敗，未找到活動，ID: " + activityId);
        }
    }

    // ======== 座位相關處理方法 ========

    private void handleGetSeats(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取座位列表，活動ID: " + activityId);

        List<Seat> seats = seatService.getSeatsByActivityId(activityId);
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, seats);
        logger.info("返回 " + seats.size() + " 個座位，活動ID: " + activityId);
    }

    private void handleGetSelections(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取選座記錄，活動ID: " + activityId);

        List<Selection> selections = seatService.getSeatSelections(activityId);
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, selections);
        logger.info("返回 " + selections.size() + " 個選座記錄，活動ID: " + activityId);
    }

    // ======== 參與者相關處理方法 ========

    private void handleGetParticipants(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取參與者列表，活動ID: " + activityId);

        List<Participant> participants = participantService.getParticipants(activityId);
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, participants);
        logger.info("返回 " + participants.size() + " 個參與者，活動ID: " + activityId);
    }

    private void handleJoinActivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("參與者加入活動，活動ID: " + activityId);

        try {
            // 使用 DTO 接收前端數據
            ParticipantCreateDto participantDto = JsonUtil.parseRequestBody(request, ParticipantCreateDto.class);

            // 添加調試信息
            logger.info("解析的參與者信息: ");
            logger.info("姓名: " + participantDto.getName());
            logger.info("聯絡方式: " + participantDto.getContact());
            logger.info("加入時間: " + participantDto.getJoinTime());

            // 使用 DTO 創建參與者
            Participant createdParticipant = participantService.joinActivity(participantDto, activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_CREATED, createdParticipant);
            logger.info("參與者成功加入活動: " + createdParticipant.getName() + "，ID: " + createdParticipant.getId());
        } catch (IllegalArgumentException e) {
            logger.warning("參與者加入活動失敗: " + e.getMessage());
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.severe("參與者加入活動時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void handleSelectSeat(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        String seatId = UrlUtil.extractSeatId(request.getRequestURI());
        logger.info("選擇座位，活動ID: " + activityId + "，座位ID: " + seatId);

        Map<String, Object> requestBody = JsonUtil.parseRequestBody(request, Map.class);
        int participantId = ((Number) requestBody.get("userId")).intValue();

        Selection selection = participantService.selectSeat(activityId, participantId, seatId);
        ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, selection);
        logger.info("成功選擇座位，參與者ID: " + participantId + "，座位ID: " + seatId);
    }

    private void handleGetUserSelection(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        int participantId = UrlUtil.extractParticipantId(request.getRequestURI());
        logger.info("獲取參與者選座記錄，活動ID: " + activityId + "，參與者ID: " + participantId);

        Selection selection = participantService.getUserSelection(activityId, participantId);

        if (selection != null) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, selection);
            logger.info("找到選座記錄，座位ID: " + selection.getSeatId());
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到選座記錄");
            logger.info("未找到選座記錄");
        }
    }

    private void handleCancelSeatSelection(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        int participantId = UrlUtil.extractParticipantId(request.getRequestURI());
        logger.info("取消選座，活動ID: " + activityId + "，參與者ID: " + participantId);

        boolean success = participantService.cancelSeatSelection(activityId, participantId);
        if (success) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, "已成功取消選座");
            logger.info("成功取消選座");
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到選座記錄");
            logger.warning("取消選座失敗，未找到記錄");
        }
    }

    // ======== 抽籤相關處理方法 ========


    private void handleEndSelectionEarly(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("提前結束選位請求，活動ID: " + activityId);

        try {
            Activity updatedActivity = activityService.endSelectionEarly(activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, updatedActivity);
            logger.info("成功提前結束選位，活動ID: " + activityId);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "提前結束選位失敗", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "提前結束選位失敗: " + e.getMessage());
        }
    }


    private void handleRerunLottery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("重新抽籤請求，活動ID: " + activityId);

        try {
            Map<String, Object> result = activityService.rerunLottery(activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
            logger.info("重新抽籤成功完成，活動ID: " + activityId);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "重新抽籤失敗", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "重新抽籤失敗: " + e.getMessage());
        }
    }













    private void handleRunLottery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("執行抽籤請求，活動ID: " + activityId);

        try {
            // 改用 LotteryService 而不是 ActivityService
            boolean success = lotteryService.runLottery(activityId);

            if (success) {
                // 獲取詳細結果
                Map<String, Object> result = lotteryService.getLotteryResult(activityId);
                ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
                logger.info("抽籤成功完成，活動ID: " + activityId);
            } else {
                ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "抽籤執行失敗");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "抽籤失敗", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "抽籤失敗: " + e.getMessage());
        }
    }

    private void handleGetLotteryResult(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取抽籤結果，活動ID: " + activityId);

        // 改用 LotteryService
        Map<String, Object> result = lotteryService.getLotteryResult(activityId);

        if (result != null) {
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, result);
            logger.info("成功返回抽籤結果");
        } else {
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到抽籤結果");
            logger.info("未找到抽籤結果");
        }
    }

    private void handleGetLotteryResults(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int activityId = UrlUtil.extractActivityId(request.getRequestURI());
        logger.info("獲取詳細抽籤結果請求，活動ID: " + activityId);

        try {
            // 獲取詳細結果列表
            List<LotteryResult> results = activityService.getLotteryResults(activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, results);
            logger.info("成功獲取抽籤結果，活動ID: " + activityId + "，結果數量: " + results.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "獲取抽籤結果失敗", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "獲取抽籤結果失敗: " + e.getMessage());
        }
    }

}