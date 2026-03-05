package com.seatlottery.service;

import com.seatlottery.dao.*;
import com.seatlottery.dto.ActivityCreateDto;
import com.seatlottery.dto.SeatLayoutDto;
import com.seatlottery.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActivityService {
    private static final Logger logger = Logger.getLogger(ActivityService.class.getName());
    private ActivityDao activityDao;
    private SeatDao seatDao;
    private ParticipantDao participantDao;
    private SelectionDao selectionDao;
    private LotteryResultDao lotteryResultDao;

    public ActivityService() {
        this.activityDao = new ActivityDao();
        this.seatDao = new SeatDao();
        this.participantDao = new ParticipantDao();
        this.selectionDao = new SelectionDao();
        this.lotteryResultDao = new LotteryResultDao();
    }


    public Activity endSelectionEarly(int activityId) throws SQLException {
        logger.info("提前結束選位，活動ID: " + activityId);

        Activity activity = getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        if (!"active".equals(activity.getStatus())) {
            throw new SQLException("只有進行中的活動可以提前結束選位");
        }

        // 將選位結束時間設為當前時間，狀態改為 closed
        Date now = new Date();
        activity.setSelectionEndTime(now);
        activity.setStatus("closed");

        // 更新到數據庫
        boolean updated = activityDao.updateActivity(activity);
        if (!updated) {
            throw new SQLException("更新活動失敗");
        }

        logger.info("成功提前結束選位，活動ID: " + activityId);
        return getActivityById(activityId);
    }

    /**
     * 執行抽籤 - 根據您的 DAO 結構調整
     */
    public Map<String, Object> runLottery(int activityId) throws SQLException {
        logger.info("=== 開始執行抽籤，活動ID: " + activityId + " ===");

        Activity activity = getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        if (!("active".equals(activity.getStatus()) || "closed".equals(activity.getStatus()))) {
            throw new SQLException("活動必須處於進行中或選位已結束狀態才能進行抽籤");
        }

        Date now = new Date();
        if ("active".equals(activity.getStatus()) && now.before(activity.getSelectionEndTime())) {
            throw new SQLException("選位時間尚未結束，無法進行抽籤");
        }

        try {
            // 1. 獲取所有座位
            List<Seat> allSeats = seatDao.getSeatsByActivityId(activityId);
            logger.info("獲取到座位數: " + allSeats.size());
            for (Seat seat : allSeats) {
                logger.info("座位: " + seat.getSeatId() + " (標籤: " + seat.getLabel() + ")");
            }

            // 2. 獲取所有選座記錄
            List<Selection> selections = selectionDao.getSelectionsByActivityId(activityId);
            logger.info("獲取到選座記錄數: " + selections.size());
            for (Selection selection : selections) {
                logger.info("選座記錄: " + selection.getUserName() + " -> " + selection.getSeatId());
            }

            // 3. 先清除舊的抽籤結果
            try {
                lotteryResultDao.deleteResultsByActivityId(activityId);
                logger.info("清除舊的抽籤結果完成");
            } catch (Exception e) {
                logger.warning("清除舊抽籤結果時出錯: " + e.getMessage());
            }

            // 4. 分析衝突情況
            Map<String, List<Selection>> conflictMap = new HashMap<>();

            // 按座位分組
            for (Selection selection : selections) {
                String seatId = selection.getSeatId();
                conflictMap.computeIfAbsent(seatId, k -> new ArrayList<>()).add(selection);
            }

            logger.info("=== 衝突分析結果 ===");
            for (Map.Entry<String, List<Selection>> entry : conflictMap.entrySet()) {
                String seatId = entry.getKey();
                List<Selection> seatSelections = entry.getValue();
                if (seatSelections.size() > 1) {
                    logger.info("衝突座位 " + seatId + " 有 " + seatSelections.size() + " 人競爭:");
                    for (Selection s : seatSelections) {
                        logger.info("  - " + s.getUserName());
                    }
                } else {
                    logger.info("無衝突座位 " + seatId + ": " + seatSelections.get(0).getUserName());
                }
            }

            // 5. 第一階段：處理所有座位選擇
            Set<String> occupiedSeats = new HashSet<>();
            List<LotteryResult> results = new ArrayList<>();
            List<Selection> unassignedSelections = new ArrayList<>();

            logger.info("=== 第一階段：處理座位分配 ===");

            for (Map.Entry<String, List<Selection>> entry : conflictMap.entrySet()) {
                String seatId = entry.getKey();
                List<Selection> seatSelections = entry.getValue();

                if (seatSelections.size() == 1) {
                    // 無衝突，直接分配
                    Selection selection = seatSelections.get(0);
                    LotteryResult result = createLotteryResult(activityId, selection, seatId, false);
                    results.add(result);
                    occupiedSeats.add(seatId);
                    logger.info("✓ 直接分配: " + selection.getUserName() + " -> " + seatId);

                } else {
                    // 有衝突，進行抽籤
                    logger.info("⚡ 處理衝突座位 " + seatId + " (" + seatSelections.size() + " 人競爭)");

                    // 隨機選擇獲勝者
                    Collections.shuffle(seatSelections);
                    Selection winner = seatSelections.get(0);

                    // 創建獲勝者結果
                    LotteryResult winnerResult = createLotteryResult(activityId, winner, seatId, false);
                    winnerResult.setConflictCount(seatSelections.size());
                    results.add(winnerResult);
                    occupiedSeats.add(seatId);
                    logger.info("🏆 獲勝者: " + winner.getUserName() + " 獲得座位 " + seatId);

                    // 失敗者加入待重新分配列表
                    for (int i = 1; i < seatSelections.size(); i++) {
                        Selection failedSelection = seatSelections.get(i);
                        unassignedSelections.add(failedSelection);
                        logger.info("😞 失敗者: " + failedSelection.getUserName() + " (原選 " + failedSelection.getSeatId() + ") 待重新分配");
                    }
                }
            }

            logger.info("=== 第一階段完成 ===");
            logger.info("已佔用座位數: " + occupiedSeats.size());
            logger.info("已佔用座位列表: " + occupiedSeats);
            logger.info("待重新分配人數: " + unassignedSelections.size());
            for (Selection s : unassignedSelections) {
                logger.info("  待分配: " + s.getUserName());
            }

            // 6. 第二階段：為失敗者重新分配空座位
            if (!unassignedSelections.isEmpty()) {
                logger.info("=== 第二階段：重新分配空座位 ===");

                // 計算所有空座位
                List<String> availableSeats = new ArrayList<>();
                for (Seat seat : allSeats) {
                    String seatId = seat.getSeatId();
                    if (!occupiedSeats.contains(seatId)) {
                        availableSeats.add(seatId);
                        logger.info("空座位: " + seatId);
                    }
                }

                logger.info("可用空座位總數: " + availableSeats.size());
                logger.info("需要重新分配人數: " + unassignedSelections.size());

                if (availableSeats.isEmpty()) {
                    logger.warning("❌ 沒有空座位可供重新分配！");
                } else {
                    // 隨機排列空座位
                    Collections.shuffle(availableSeats);
                    logger.info("空座位隨機排列完成");

                    // 為每個失敗者分配座位
                    for (int i = 0; i < unassignedSelections.size(); i++) {
                        Selection failedSelection = unassignedSelections.get(i);

                        if (i < availableSeats.size()) {
                            // 有空座位可以分配
                            String assignedSeatId = availableSeats.get(i);
                            LotteryResult reassignedResult = createLotteryResult(activityId, failedSelection, assignedSeatId, true);
                            reassignedResult.setConflictCount(1);
                            results.add(reassignedResult);
                            occupiedSeats.add(assignedSeatId);

                            logger.info("✅ 重新分配: " + failedSelection.getUserName() +
                                    " 從 " + failedSelection.getSeatId() + " 重新分配到 " + assignedSeatId);
                            logger.info("    isReassigned = " + reassignedResult.isReassigned());

                        } else {
                            // 沒有空座位
                            LotteryResult noSeatResult = createLotteryResult(activityId, failedSelection, null, false);
                            noSeatResult.setConflictCount(1);
                            results.add(noSeatResult);
                            logger.warning("❌ 無座位: " + failedSelection.getUserName() + " 無法分配到座位");
                        }
                    }
                }
            } else {
                logger.info("=== 跳過第二階段：沒有人需要重新分配 ===");
            }

            logger.info("=== 準備保存結果 ===");
            logger.info("總結果數: " + results.size());

            // 7. 詳細檢查每個結果
            for (int i = 0; i < results.size(); i++) {
                LotteryResult result = results.get(i);
                logger.info("結果 " + (i+1) + ": " +
                        result.getParticipantName() +
                        " | 原選: " + result.getOriginalSeatId() +
                        " | 分配: " + result.getAssignedSeatId() +
                        " | 重新分配: " + result.isReassigned() +
                        " | 成功: " + result.isSuccess());
            }

            // 8. 保存所有抽籤結果
            int savedCount = 0;
            for (LotteryResult result : results) {
                try {
                    boolean saved = lotteryResultDao.createLotteryResult(result);
                    if (saved) {
                        savedCount++;
                        logger.info("保存成功: " + result.getParticipantName());
                    } else {
                        logger.warning("保存失敗: " + result.getParticipantName());
                    }
                } catch (Exception e) {
                    logger.severe("保存錯誤: " + result.getParticipantName() + " - " + e.getMessage());
                }
            }

            logger.info("成功保存 " + savedCount + " / " + results.size() + " 條結果");

            // 9. 更新活動狀態
            activityDao.updateActivityStatus(activityId, "completed");

            // 10. 統計結果
            long successfulAssignments = results.stream().filter(r -> r.isSuccess()).count();
            long failedAssignments = results.stream().filter(r -> !r.isSuccess()).count();
            long reassignments = results.stream().filter(r -> r.isReassigned()).count();

            logger.info("=== 最終統計 ===");
            logger.info("成功分配: " + successfulAssignments);
            logger.info("失敗分配: " + failedAssignments);
            logger.info("重新分配: " + reassignments);

            Map<String, Object> summary = new HashMap<>();
            summary.put("activityId", activityId);
            summary.put("totalParticipants", results.size());
            summary.put("successfulAssignments", successfulAssignments);
            summary.put("failedAssignments", failedAssignments);
            summary.put("reassignments", reassignments);
            summary.put("results", results);
            summary.put("message", "抽籤完成！成功分配 " + successfulAssignments + " 個座位，重新分配 " + reassignments + " 個座位");

            return summary;

        } catch (Exception e) {
            logger.severe("抽籤錯誤: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("抽籤失敗: " + e.getMessage());
        }
    }
    /**
     * 重新抽籤
     */
    public Map<String, Object> rerunLottery(int activityId) throws SQLException {
        logger.info("重新執行抽籤，活動ID: " + activityId);

        // 重置活動狀態為 closed，允許重新抽籤
        Activity activity = getActivityById(activityId);
        if (activity != null && "completed".equals(activity.getStatus())) {
            activity.setStatus("closed");
            activityDao.updateActivityStatus(activityId, "closed");
            logger.info("將活動狀態從 completed 重置為 closed");
        }

        return runLottery(activityId);
    }


    // 確保創建方法正確設置 reassigned 標記
    private LotteryResult createLotteryResult(int activityId, Selection selection, String assignedSeatId, boolean isReassigned) {
        LotteryResult result = new LotteryResult();
        result.setActivityId(activityId);
        result.setParticipantId(selection.getParticipantId());
        result.setParticipantName(selection.getUserName());
        result.setOriginalSeatId(selection.getSeatId());
        result.setAssignedSeatId(assignedSeatId);
        result.setLotteryTime(new Date());

        // 明確設置重新分配標記
        result.setReassigned(isReassigned);
        logger.info("創建結果: " + selection.getUserName() +
                " | 原選: " + selection.getSeatId() +
                " | 分配: " + assignedSeatId +
                " | isReassigned: " + isReassigned);

        // 設置 seatId 欄位（兼容性）
        result.setSeatId(assignedSeatId != null ? assignedSeatId : "");

        // 設置成功狀態
        result.setSuccess(assignedSeatId != null && !assignedSeatId.isEmpty());

        // 設置狀態
        if (assignedSeatId != null && !assignedSeatId.isEmpty()) {
            result.setStatus("assigned");
        } else {
            result.setStatus("no_seat");
        }

        // 預設衝突數
        result.setConflictCount(1);

        return result;
    }
    /**
     * 獲取抽籤結果
     */
    public List<LotteryResult> getLotteryResults(int activityId) throws SQLException {
        return lotteryResultDao.getResultsByActivityId(activityId);
    }

    /**
     * 獲取獲勝者列表（成功分配到座位的參與者）
     */
    public List<LotteryResult> getWinners(int activityId) throws SQLException {
        List<LotteryResult> allResults = getLotteryResults(activityId);
        return allResults.stream()
                .filter(LotteryResult::isSuccess)
                .collect(Collectors.toList());
    }

    /**
     * 獲取活動統計信息
     */
    public Map<String, Object> getActivityStatistics(int activityId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        Activity activity = getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        // 基本信息
        stats.put("activity", activity);

        // 參與者統計
        List<Participant> participants = participantDao.getParticipantsByActivityId(activityId);
        stats.put("totalParticipants", participants.size());

        // 座位統計
        List<Seat> seats = seatDao.getSeatsByActivityId(activityId);
        long availableSeats = seats.stream().filter(Seat::isAvailable).count();
        stats.put("totalSeats", seats.size());
        stats.put("availableSeats", availableSeats);

        // 選座統計
        List<Selection> selections = selectionDao.getSelectionsByActivityId(activityId);
        stats.put("totalSelections", selections.size());

        // 計算衝突統計
        Map<String, List<Selection>> seatGroups = selections.stream()
                .collect(Collectors.groupingBy(Selection::getSeatId));

        long conflictSeats = seatGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .count();

        stats.put("conflictSeats", conflictSeats);

        // 抽籤結果統計（如果已抽籤）
        if ("completed".equals(activity.getStatus())) {
            List<LotteryResult> results = getLotteryResults(activityId);
            long winners = results.stream().filter(LotteryResult::isSuccess).count();
            stats.put("winners", winners);
            stats.put("lotteryCompleted", true);
        } else {
            stats.put("lotteryCompleted", false);
        }

        return stats;
    }
    public void updateActivityStatus(Activity activity) {
        if (activity == null) return;

        String currentStatus = activity.getStatus();
        Date now = new Date();
        Date startTime = activity.getSelectionStartTime();
        Date endTime = activity.getSelectionEndTime();

        logger.info("檢查活動狀態 - 活動ID: " + activity.getId());
        logger.info("當前時間: " + now);
        logger.info("當前狀態: " + currentStatus);
        logger.info("開始時間: " + startTime);
        logger.info("結束時間: " + endTime);

        String newStatus = currentStatus;

        try {
            // 重要：如果狀態是 closed，不要自動改回 active
            // closed 狀態表示管理員手動提前結束，應該保持這個狀態
            if ("closed".equals(currentStatus)) {
                logger.info("活動狀態為 closed（手動提前結束），保持此狀態");
                return;
            }

            // 如果狀態是 completed，也不要修改
            if ("completed".equals(currentStatus)) {
                logger.info("活動已完成，保持 completed 狀態");
                return;
            }

            // 只對 pending 和 active 狀態進行自動更新
            if ("pending".equals(currentStatus)) {
                if (now.after(startTime)) {
                    newStatus = "active";
                    logger.info("活動自動開始");
                }
            } else if ("active".equals(currentStatus)) {
                // 只有在自然到達結束時間時才自動變為 closed
                if (now.after(endTime)) {
                    newStatus = "closed";
                    logger.info("活動自然結束");
                }
            }

            // 只有狀態真的需要改變時才更新數據庫
            if (!newStatus.equals(currentStatus)) {
                logger.info("活動狀態變化: " + currentStatus + " -> " + newStatus);

                boolean updated = activityDao.updateActivityStatus(activity.getId(), newStatus);
                if (updated) {
                    activity.setStatus(newStatus);
                    logger.info("活動狀態更新成功");
                } else {
                    logger.warning("活動狀態更新失敗");
                }
            } else {
                logger.info("活動狀態無需變化，保持: " + currentStatus);
            }

        } catch (SQLException e) {
            logger.severe("更新活動狀態時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 批量更新所有活動狀態
     */
    public void updateAllActivityStatuses() throws SQLException {
        logger.info("開始批量更新所有活動狀態");
        List<Activity> activities = activityDao.getAllActivities();

        for (Activity activity : activities) {
            updateActivityStatus(activity);
        }

        logger.info("完成批量更新 " + activities.size() + " 個活動的狀態");
    }

    public List<Activity> getAllActivities() throws SQLException {
        logger.info("獲取所有活動");
        List<Activity> activities = activityDao.getAllActivities();

        // 更新每個活動的狀態
        for (Activity activity : activities) {
            updateActivityStatus(activity);
        }

        return activities;
    }

    public Activity getActivityById(int id) throws SQLException {
        logger.info("獲取活動詳情，ID: " + id);
        Activity activity = activityDao.getActivityById(id);

        if (activity != null) {
            // 更新活動狀態
            updateActivityStatus(activity);

            // 獲取座位佈局
            List<Seat> seats = seatDao.getSeatsByActivityId(id);
            activity.setSeatLayout(seats);
            logger.info("獲取到活動: " + activity.getName() + "，包含 " + seats.size() + " 個座位，狀態: " + activity.getStatus());
        }

        return activity;
    }

    // ... 其他現有方法保持不變

    /**
     * 從 DTO 創建活動
     */
    public Activity createActivity(ActivityCreateDto activityDto) throws SQLException {
        logger.info("開始創建活動: " + activityDto.getName());

        // 將 DTO 轉換為實體
        Activity activity = convertDtoToActivity(activityDto);

        // 計算總座位數
        if (activity.getTotalSeats() <= 0) {
            int totalSeats = activity.getRows() * activity.getCols();
            activity.setTotalSeats(totalSeats);
        }

        // 設置初始狀態
        updateActivityStatus(activity);

        // 創建活動
        Activity createdActivity = activityDao.createActivity(activity);
        logger.info("活動創建成功，ID: " + createdActivity.getId());

        // 處理座位佈局
        if (activityDto.getSeatLayout() != null && !activityDto.getSeatLayout().isEmpty()) {
            logger.info("開始創建座位佈局，座位數量: " + activityDto.getSeatLayout().size());

            List<Seat> seatsToCreate = new ArrayList<>();

            for (SeatLayoutDto seatDto : activityDto.getSeatLayout()) {
                Seat seat = new Seat();
                seat.setActivityId(createdActivity.getId());
                seat.setSeatId(seatDto.getPositionId());
                seat.setRow(seatDto.getRow());
                seat.setCol(seatDto.getCol());
                seat.setLabel(seatDto.getLabel());
                seat.setAvailable(seatDto.isAvailable());

                seatsToCreate.add(seat);
            }

            // 批量創建座位
            int createdSeatsCount = seatDao.createBatchSeats(seatsToCreate);
            logger.info("成功創建 " + createdSeatsCount + " 個座位");

            // 設置創建的座位到活動中
            createdActivity.setSeatLayout(seatsToCreate);
        } else {
            // 如果沒有提供座位佈局，創建默認佈局
            logger.info("沒有提供座位佈局，創建默認佈局");
            List<Seat> defaultSeats = createDefaultSeatLayout(createdActivity);
            createdActivity.setSeatLayout(defaultSeats);
        }

        logger.info("活動創建完成: " + createdActivity.getName());
        return createdActivity;
    }

    /**
     * 將 ActivityCreateDto 轉換為 Activity 實體
     */
    private Activity convertDtoToActivity(ActivityCreateDto dto) {
        Activity activity = new Activity();
        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setSelectionStartTime(dto.getSelectionStartTime());
        activity.setSelectionEndTime(dto.getSelectionEndTime());
        activity.setRows(dto.getRows());
        activity.setCols(dto.getCols());
        activity.setTotalSeats(dto.getTotalSeats());
        activity.setStatus(dto.getStatus() != null ? dto.getStatus() : "pending");
        activity.setParticipantCount(dto.getParticipantCount());

        return activity;
    }

    /**
     * 創建默認座位佈局
     */
    private List<Seat> createDefaultSeatLayout(Activity activity) throws SQLException {
        List<Seat> seats = new ArrayList<>();

        for (int row = 0; row < activity.getRows(); row++) {
            for (int col = 0; col < activity.getCols(); col++) {
                Seat seat = new Seat();
                seat.setActivityId(activity.getId());
                seat.setSeatId(row + "-" + col);
                seat.setRow(row);
                seat.setCol(col);
                seat.setLabel((row + 1) + "-" + (char)('A' + col));
                seat.setAvailable(true);

                seats.add(seat);
            }
        }

        // 批量創建座位
        int createdCount = seatDao.createBatchSeats(seats);
        logger.info("創建默認座位佈局，共 " + createdCount + " 個座位");

        return seats;
    }

    public Activity updateActivity(Activity activity) throws SQLException {
        logger.info("更新活動，ID: " + activity.getId());

        // 更新活動狀態
        updateActivityStatus(activity);

        boolean updated = activityDao.updateActivity(activity);
        if (updated) {
            // 返回更新後的活動
            return getActivityById(activity.getId());
        }

        return null;
    }

    public boolean deleteActivity(int id) throws SQLException {
        logger.info("刪除活動，ID: " + id);

        // 先刪除相關座位（由於外鍵約束，這可能會自動處理）
        seatDao.deleteSeatsByActivityId(id);

        // 刪除活動
        return activityDao.deleteActivity(id);
    }

    public Activity resumeSelection(int activityId) throws SQLException {
        logger.info("恢復選位，活動ID: " + activityId);

        Activity activity = getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        if (!"closed".equals(activity.getStatus())) {
            throw new SQLException("只有選位已結束的活動可以恢復選位，當前狀態: " + activity.getStatus());
        }

        // 檢查是否已經抽籤
        try {
            List<LotteryResult> existingResults = lotteryResultDao.getResultsByActivityId(activityId);
            if (!existingResults.isEmpty()) {
                throw new SQLException("活動已進行抽籤，無法恢復選位");
            }
        } catch (Exception e) {
            logger.warning("查詢抽籤結果時出錯: " + e.getMessage());
        }

        try {
            // 使用 DAO 方法恢復選位（默認延長1小時）
            boolean updated = activityDao.resumeSelection(activityId);

            if (!updated) {
                throw new SQLException("恢復選位失敗：沒有影響到任何行");
            }

            logger.info("成功恢復選位，活動ID: " + activityId + "，狀態已更新為 active，延長1小時");

            // 重新獲取活動信息以確認更新
            Activity updatedActivity = getActivityById(activityId);
            if (updatedActivity != null) {
                logger.info("確認更新後狀態: " + updatedActivity.getStatus() + "，結束時間: " + updatedActivity.getSelectionEndTime());
            }

            return updatedActivity;

        } catch (SQLException e) {
            logger.severe("恢復選位時發生數據庫錯誤: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 恢復選位並指定新的結束時間
     */
    public Activity resumeSelection(int activityId, Date newEndTime) throws SQLException {
        logger.info("恢復選位，活動ID: " + activityId + "，新結束時間: " + newEndTime);

        Activity activity = getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        if (!"closed".equals(activity.getStatus())) {
            throw new SQLException("只有選位已結束的活動可以恢復選位，當前狀態: " + activity.getStatus());
        }

        // 檢查新結束時間必須是未來的時間
        Date now = new Date();
        if (newEndTime.before(now)) {
            throw new SQLException("新的結束時間必須是未來的時間");
        }

        // 檢查是否已經抽籤
        try {
            List<LotteryResult> existingResults = lotteryResultDao.getResultsByActivityId(activityId);
            if (!existingResults.isEmpty()) {
                throw new SQLException("活動已進行抽籤，無法恢復選位");
            }
        } catch (Exception e) {
            logger.warning("查詢抽籤結果時出錯: " + e.getMessage());
        }

        try {
            // 使用 DAO 方法恢復選位並指定時間
            boolean updated = activityDao.resumeSelection(activityId, newEndTime);

            if (!updated) {
                throw new SQLException("恢復選位失敗：沒有影響到任何行");
            }

            logger.info("成功恢復選位，活動ID: " + activityId + "，新結束時間: " + newEndTime);

            // 重新獲取活動信息
            return getActivityById(activityId);

        } catch (SQLException e) {
            logger.severe("恢復選位時發生數據庫錯誤: " + e.getMessage());
            throw e;
        }
    }


}