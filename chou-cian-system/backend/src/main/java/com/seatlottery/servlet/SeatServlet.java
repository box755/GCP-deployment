package com.seatlottery.servlet;

import com.seatlottery.model.Seat;
import com.seatlottery.service.SeatService;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.seatlottery.model.Selection;


public class SeatServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SeatServlet.class.getName());
    private SeatService seatService;

    @Override
    public void init() throws ServletException {
        this.seatService = new SeatService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String uri = request.getRequestURI();
            logger.info("處理座位請求，URI: " + uri);

            // 使用工具類從 URL 中提取活動ID
            int activityId = UrlUtil.extractActivityId(uri);
            logger.info("提取的活動ID: " + activityId);

            // 獲取座位列表
            List<Seat> seats = seatService.getSeatsByActivityId(activityId);
            ResponseUtil.writeJsonResponse(response, HttpServletResponse.SC_OK, seats);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "解析活動ID錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "無效的活動ID: " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "資料庫錯誤", e);
            ResponseUtil.writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫錯誤: " + e.getMessage());
        }
    }
}