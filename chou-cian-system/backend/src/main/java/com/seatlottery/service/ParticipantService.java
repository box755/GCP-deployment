package com.seatlottery.service;

import com.seatlottery.dao.ActivityDao;
import com.seatlottery.dao.ParticipantDao;
import com.seatlottery.dao.SelectionDao;
import com.seatlottery.dao.SeatDao;
import com.seatlottery.dto.ParticipantCreateDto;
import com.seatlottery.model.Activity;
import com.seatlottery.model.Participant;
import com.seatlottery.model.Selection;
import com.seatlottery.model.Seat;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ParticipantService {
    private static final Logger logger = Logger.getLogger(ParticipantService.class.getName());
    private ParticipantDao participantDao;
    private SelectionDao selectionDao;
    private SeatDao seatDao;
    private ActivityDao activityDao;  // ⭐ 添加 ActivityDao

    public ParticipantService() {
        this.participantDao = new ParticipantDao();
        this.selectionDao = new SelectionDao();
        this.seatDao = new SeatDao();
        this.activityDao = new ActivityDao();  // ⭐ 初始化 ActivityDao
    }

    /**
     * 使用 DTO 創建參與者 - 完整版本，包含所有檢查
     */
    public Participant joinActivity(ParticipantCreateDto participantDto, int activityId) throws SQLException {
        logger.info("用戶嘗試加入活動 - 活動ID: " + activityId + ", 用戶: " + participantDto.getName());

        // 1. ⭐ 檢查活動是否存在並獲取活動信息
        Activity activity = activityDao.getActivityById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活動不存在");
        }

        // 2. ⭐ 檢查活動狀態是否允許加入
        if (!"active".equals(activity.getStatus())) {
            throw new IllegalArgumentException("活動未開放參與，目前狀態：" + activity.getStatus());
        }

        // 3. ⭐ 檢查選位時間是否有效
        Date now = new Date();
        if (now.before(activity.getSelectionStartTime())) {
            throw new IllegalArgumentException("活動尚未開始，請於選位開始時間後再試");
        }

        if (now.after(activity.getSelectionEndTime())) {
            throw new IllegalArgumentException("活動選位時間已結束");
        }

        // 4. ⭐ 檢查是否已經參與過活動（防止重複參與）
        List<Participant> existingParticipants = participantDao.getParticipantsByActivityId(activityId);
        for (Participant existing : existingParticipants) {
            if (existing.getName().equals(participantDto.getName()) &&
                    existing.getContact().equals(participantDto.getContact())) {
                logger.warning("參與者已存在: " + participantDto.getName());
                throw new IllegalArgumentException("您已經參與過此活動了");
            }
        }

        // 5. ⭐ 檢查參與人數是否超過座位數量限制
        int currentParticipantCount = existingParticipants.size();
        int totalSeats = activity.getTotalSeats();

        logger.info("座位檢查 - 目前參與人數: " + currentParticipantCount +
                ", 總座位數: " + totalSeats +
                ", 資料庫記錄的參與人數: " + activity.getParticipantCount());

        if (currentParticipantCount >= totalSeats) {
            throw new IllegalArgumentException("很抱歉，活動人數已滿！" +
                    "目前參與人數：" + currentParticipantCount + "，座位總數：" + totalSeats);
        }

        // 6. ⭐ 將 DTO 轉換為實體
        Participant participant = convertDtoToParticipant(participantDto, activityId);

        // 7. ⭐ 創建參與者記錄
        Participant createdParticipant = participantDao.createParticipant(participant);

        if (createdParticipant != null) {
            logger.info("參與者成功創建，ID: " + createdParticipant.getId());

            // 8. ⭐ 更新活動的參與者計數
            try {
                boolean updated = activityDao.incrementParticipantCount(activityId);
                if (updated) {
                    logger.info("成功更新活動參與者計數: " + (currentParticipantCount + 1));
                } else {
                    logger.warning("更新活動參與者計數失敗，但用戶已成功加入");
                }
            } catch (SQLException e) {
                logger.severe("更新活動參與者計數時發生錯誤: " + e.getMessage());
                // 不拋出異常，因為用戶已經成功加入，只是計數更新失敗
            }

            logger.info("用戶成功加入活動 - " +
                    "用戶ID: " + createdParticipant.getId() +
                    ", 目前參與人數: " + (currentParticipantCount + 1) + "/" + totalSeats);

            return createdParticipant;
        } else {
            throw new SQLException("創建參與者記錄失敗");
        }
    }

    /**
     * 傳統方式創建參與者（向後兼容）- 也需要添加相同的檢查
     */
    public Participant joinActivity(Participant participant) throws SQLException {
        logger.info("使用傳統方式加入活動，參與者: " + participant.getName());

        // 轉換為 DTO 然後調用新方法，確保邏輯一致
        ParticipantCreateDto dto = new ParticipantCreateDto();
        dto.setName(participant.getName());
        dto.setContact(participant.getContact());
        dto.setJoinTime(participant.getJoinTime());

        return joinActivity(dto, participant.getActivityId());
    }

    /**
     * 將 ParticipantCreateDto 轉換為 Participant 實體
     */
    private Participant convertDtoToParticipant(ParticipantCreateDto dto, int activityId) {
        Participant participant = new Participant();
        participant.setActivityId(activityId);
        participant.setName(dto.getName());
        participant.setContact(dto.getContact());

        // 處理加入時間
        if (dto.getJoinTime() != null) {
            participant.setJoinTime(dto.getJoinTime());
        } else {
            participant.setJoinTime(new Date()); // 使用當前時間
        }

        return participant;
    }

    // ⭐ 添加獲取活動參與者統計的方法
    public ActivityParticipantStats getParticipantStats(int activityId) throws SQLException {
        Activity activity = activityDao.getActivityById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活動不存在");
        }

        List<Participant> participants = participantDao.getParticipantsByActivityId(activityId);

        ActivityParticipantStats stats = new ActivityParticipantStats();
        stats.setActivityId(activityId);
        stats.setTotalSeats(activity.getTotalSeats());
        stats.setCurrentParticipants(participants.size());
        stats.setDatabaseParticipantCount(activity.getParticipantCount());
        stats.setAvailableSeats(activity.getTotalSeats() - participants.size());
        stats.setIsFull(participants.size() >= activity.getTotalSeats());

        return stats;
    }

    // 其他方法保持不變...
    public List<Participant> getParticipants(int activityId) throws SQLException {
        logger.info("獲取參與者列表，活動ID: " + activityId);
        return participantDao.getParticipantsByActivityId(activityId);
    }

    public Selection selectSeat(int activityId, int participantId, String seatId) throws SQLException {
        logger.info("選擇座位，活動ID: " + activityId + "，參與者ID: " + participantId + "，座位ID: " + seatId);

        // 檢查座位是否存在且可用
        Seat seat = seatDao.getSeatBySeatId(activityId, seatId);
        if (seat == null) {
            throw new IllegalArgumentException("座位不存在");
        }
        if (!seat.isAvailable()) {
            throw new IllegalArgumentException("座位不可用");
        }

        // 檢查參與者是否已經選過座位
        Selection existingSelection = selectionDao.getSelectionByParticipantId(activityId, participantId);
        if (existingSelection != null) {
            throw new IllegalArgumentException("參與者已經選過座位");
        }

        // 創建選座記錄
        Selection selection = new Selection();
        selection.setActivityId(activityId);
        selection.setParticipantId(participantId);
        selection.setSeatId(seatId);
        selection.setSelectionTime(new Date());
        selection.setStatus("confirmed");

        Selection createdSelection = selectionDao.createSelection(selection);
        logger.info("成功創建選座記錄: " + createdSelection.getId());

        return createdSelection;
    }

    public Selection getUserSelection(int activityId, int participantId) throws SQLException {
        logger.info("獲取用戶選座記錄，活動ID: " + activityId + "，參與者ID: " + participantId);
        return selectionDao.getSelectionByParticipantId(activityId, participantId);
    }

    public boolean cancelSeatSelection(int activityId, int participantId) throws SQLException {
        logger.info("取消選座，活動ID: " + activityId + "，參與者ID: " + participantId);
        return selectionDao.deleteSelection(activityId, participantId);
    }

    // ⭐ 內部類：活動參與者統計
    public static class ActivityParticipantStats {
        private int activityId;
        private int totalSeats;
        private int currentParticipants;
        private int databaseParticipantCount;
        private int availableSeats;
        private boolean isFull;

        // Getters and Setters
        public int getActivityId() { return activityId; }
        public void setActivityId(int activityId) { this.activityId = activityId; }

        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

        public int getCurrentParticipants() { return currentParticipants; }
        public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }

        public int getDatabaseParticipantCount() { return databaseParticipantCount; }
        public void setDatabaseParticipantCount(int databaseParticipantCount) { this.databaseParticipantCount = databaseParticipantCount; }

        public int getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

        public boolean isFull() { return isFull; }
        public void setIsFull(boolean isFull) { this.isFull = isFull; }

        @Override
        public String toString() {
            return "ActivityParticipantStats{" +
                    "activityId=" + activityId +
                    ", totalSeats=" + totalSeats +
                    ", currentParticipants=" + currentParticipants +
                    ", databaseParticipantCount=" + databaseParticipantCount +
                    ", availableSeats=" + availableSeats +
                    ", isFull=" + isFull +
                    '}';
        }
    }
}