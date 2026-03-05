package com.seatlottery.service;

import com.seatlottery.dao.SeatDao;
import com.seatlottery.dao.SelectionDao;  // 添加 SelectionDao
import com.seatlottery.model.Seat;
import com.seatlottery.model.Selection;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class SeatService {
    private static final Logger logger = Logger.getLogger(SeatService.class.getName());
    private SeatDao seatDao;
    private SelectionDao selectionDao;  // 添加 SelectionDao

    public SeatService() {
        this.seatDao = new SeatDao();
        this.selectionDao = new SelectionDao();  // 初始化 SelectionDao
    }

    public List<Seat> getSeatsByActivityId(int activityId) throws SQLException {
        logger.info("獲取座位列表，活動ID: " + activityId);
        return seatDao.getSeatsByActivityId(activityId);
    }

    public List<Selection> getSeatSelections(int activityId) throws SQLException {
        logger.info("獲取選座記錄，活動ID: " + activityId);
        return selectionDao.getSelectionsByActivityId(activityId);  // 使用 SelectionDao
    }

    public Seat getSeatBySeatId(int activityId, String seatId) throws SQLException {
        logger.info("獲取特定座位，活動ID: " + activityId + "，座位ID: " + seatId);
        return seatDao.getSeatBySeatId(activityId, seatId);
    }

    public boolean updateSeatAvailability(int activityId, String seatId, boolean available) throws SQLException {
        logger.info("更新座位可用性，活動ID: " + activityId + "，座位ID: " + seatId + "，可用: " + available);
        return seatDao.updateSeatAvailability(activityId, seatId, available);
    }

    public int createBatchSeats(List<Seat> seats) throws SQLException {
        logger.info("批量創建座位，數量: " + seats.size());
        return seatDao.createBatchSeats(seats);
    }

    public boolean deleteSeatsByActivityId(int activityId) throws SQLException {
        logger.info("刪除活動相關座位，活動ID: " + activityId);
        return seatDao.deleteSeatsByActivityId(activityId);
    }
}