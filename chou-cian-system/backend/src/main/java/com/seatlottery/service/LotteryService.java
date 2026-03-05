package com.seatlottery.service;

import com.seatlottery.dao.*;
import com.seatlottery.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LotteryService {
    private static final Logger logger = Logger.getLogger(LotteryService.class.getName());

    private ActivityDao activityDao;
    private SeatDao seatDao;
    private ParticipantDao participantDao;
    private SelectionDao selectionDao;
    private LotteryResultDao lotteryResultDao;

    public LotteryService() {
        this.activityDao = new ActivityDao();
        this.seatDao = new SeatDao();
        this.participantDao = new ParticipantDao();
        this.selectionDao = new SelectionDao();
        this.lotteryResultDao = new LotteryResultDao();
    }

    public boolean runLottery(int activityId) throws SQLException {
        logger.info("=== 開始執行抽籤，活動ID: " + activityId + " ===");

        // 1. 獲取活動資訊
        Activity activity = activityDao.getActivityById(activityId);
        if (activity == null) {
            throw new SQLException("活動不存在");
        }

        if (!"active".equals(activity.getStatus()) && !"closed".equals(activity.getStatus())) {
            throw new SQLException("活動狀態不允許抽籤，目前狀態: " + activity.getStatus());
        }

        // 2. 獲取所有座位並分離可用/不可用座位
        List<Seat> allSeats = seatDao.getSeatsByActivityId(activityId);

        // 可用座位集合
        Set<String> availableSeatIds = allSeats.stream()
                .filter(Seat::isAvailable)
                .map(Seat::getSeatId)
                .collect(Collectors.toSet());

        logger.info("總座位數: " + allSeats.size() + ", 可用座位數: " + availableSeatIds.size());
        logger.info("可用座位列表: " + availableSeatIds);

        // 建立座位ID到座位對象的映射
        Map<String, Seat> seatMap = new HashMap<>();
        Map<String, String> seatLabels = new HashMap<>();
        for (Seat seat : allSeats) {
            seatMap.put(seat.getSeatId(), seat);
            seatLabels.put(seat.getSeatId(), seat.getLabel());
        }

        // 3. 獲取所有選位記錄
        List<Selection> allSelections = selectionDao.getSelectionsByActivityId(activityId);
        logger.info("總選位記錄數: " + allSelections.size());

        // 過濾掉選擇不可用座位的記錄
        List<Selection> validSelections = allSelections.stream()
                .filter(selection -> {
                    boolean isValid = availableSeatIds.contains(selection.getSeatId());
                    if (!isValid) {
                        logger.warning("過濾掉無效選位: 用戶ID=" + selection.getParticipantId() +
                                ", 座位ID=" + selection.getSeatId() + " (座位不可用)");
                    }
                    return isValid;
                })
                .collect(Collectors.toList());

        logger.info("有效選位記錄數: " + validSelections.size());

        // 4. 獲取參與者信息
        List<Participant> participants = participantDao.getParticipantsByActivityId(activityId);
        Map<Integer, Participant> participantMap = new HashMap<>();
        for (Participant p : participants) {
            participantMap.put(p.getId(), p);
        }

        // 5. 分組處理選位衝突（只處理可用座位的選位）
        Map<String, List<Selection>> seatConflicts = new HashMap<>();
        for (Selection selection : validSelections) {
            seatConflicts.computeIfAbsent(selection.getSeatId(), k -> new ArrayList<>()).add(selection);
        }

        logger.info("=== 衝突分析 ===");
        for (Map.Entry<String, List<Selection>> entry : seatConflicts.entrySet()) {
            String seatId = entry.getKey();
            List<Selection> conflictSelections = entry.getValue();
            logger.info("座位 " + seatId + " (" + seatLabels.get(seatId) + ") 有 " +
                    conflictSelections.size() + " 人競爭");
        }

        // 6. 清除舊的抽籤結果
        lotteryResultDao.clearLotteryResults(activityId);
        logger.info("已清除舊的抽籤結果");

        // 7. 處理抽籤並產生結果
        List<LotteryResult> results = new ArrayList<>();
        Date lotteryTime = new Date();

        // 處理有選座的參與者
        for (Map.Entry<String, List<Selection>> entry : seatConflicts.entrySet()) {
            String seatId = entry.getKey();
            List<Selection> conflictSelections = entry.getValue();
            String seatLabel = seatLabels.get(seatId);

            // 再次確認座位可用性（雙重檢查）
            if (!availableSeatIds.contains(seatId)) {
                logger.warning("跳過不可用座位: " + seatId + " (" + seatLabel + ")");

                // 為選擇不可用座位的用戶創建失敗記錄
                for (Selection selection : conflictSelections) {
                    Participant participant = participantMap.get(selection.getParticipantId());
                    LotteryResult failedResult = createLotteryResult(
                            activityId, participant, seatId, seatId, false, 0, false, lotteryTime
                    );
                    results.add(failedResult);

                    logger.info("創建失敗記錄: 用戶=" + participant.getName() +
                            ", 原因=座位不可用");
                }
                continue;
            }

            // 如果只有一個人選擇該座位，直接分配
            if (conflictSelections.size() == 1) {
                Selection selection = conflictSelections.get(0);
                Participant participant = participantMap.get(selection.getParticipantId());

                LotteryResult result = createLotteryResult(
                        activityId, participant, seatId, seatId, true, 0, false, lotteryTime
                );
                results.add(result);

                logger.info("✓ 直接分配: " + participant.getName() +
                        " -> 座位=" + seatId + " (" + seatLabel + ")");
            }
            // 如果多人選擇同一座位，隨機抽籤
            else if (conflictSelections.size() > 1) {
                logger.info("⚡ 競爭座位 " + seatId + " (" + seatLabel + ") 有 " +
                        conflictSelections.size() + " 人競爭");

                // 隨機選擇獲勝者
                Collections.shuffle(conflictSelections);
                Selection winner = conflictSelections.get(0);
                Participant winnerParticipant = participantMap.get(winner.getParticipantId());

                // 設置獲勝者結果
                LotteryResult winnerResult = createLotteryResult(
                        activityId, winnerParticipant, seatId, seatId, true,
                        conflictSelections.size() - 1, false, lotteryTime
                );
                results.add(winnerResult);

                logger.info("🏆 獲勝者: " + winnerParticipant.getName() +
                        " 獲得座位=" + seatId + " (" + seatLabel + ")");

                // 設置落選者結果
                for (int i = 1; i < conflictSelections.size(); i++) {
                    Selection loser = conflictSelections.get(i);
                    Participant loserParticipant = participantMap.get(loser.getParticipantId());

                    LotteryResult loserResult = createLotteryResult(
                            activityId, loserParticipant, seatId, null, false,
                            conflictSelections.size() - 1, false, lotteryTime
                    );
                    results.add(loserResult);

                    logger.info("😞 失敗者: " + loserParticipant.getName() +
                            " 未獲得座位=" + seatId + " (" + seatLabel + ")");
                }
            }
        }

        // 8. 處理沒有選座的參與者
        Set<Integer> participantsWithSelections = validSelections.stream()
                .map(Selection::getParticipantId)
                .collect(Collectors.toSet());

        for (Participant participant : participants) {
            if (!participantsWithSelections.contains(participant.getId())) {
                LotteryResult noSelectionResult = createLotteryResult(
                        activityId, participant, null, null, false, 0, false, lotteryTime
                );
                results.add(noSelectionResult);

                logger.info("❌ 未選座: " + participant.getName() + " (沒有選擇座位)");
            }
        }

        // 9. 處理重新分配邏輯
        List<LotteryResult> failedResults = results.stream()
                .filter(r -> !r.isSuccess() && r.getOriginalSeatId() != null)
                .collect(Collectors.toList());

        Set<String> assignedSeatIds = results.stream()
                .filter(LotteryResult::isSuccess)
                .map(LotteryResult::getAssignedSeatId)
                .collect(Collectors.toSet());

        List<String> emptySeatIds = availableSeatIds.stream()
                .filter(seatId -> !assignedSeatIds.contains(seatId))
                .collect(Collectors.toList());

        logger.info("=== 重新分配階段 ===");
        logger.info("失敗人數: " + failedResults.size() + ", 空座位數: " + emptySeatIds.size());

        if (!failedResults.isEmpty() && !emptySeatIds.isEmpty()) {
            Collections.shuffle(emptySeatIds);
            Collections.shuffle(failedResults);

            int reassignCount = Math.min(failedResults.size(), emptySeatIds.size());
            logger.info("可重新分配人數: " + reassignCount);

            for (int i = 0; i < reassignCount; i++) {
                LotteryResult failedResult = failedResults.get(i);
                String newSeatId = emptySeatIds.get(i);

                // 更新結果
                failedResult.setAssignedSeatId(newSeatId);
                failedResult.setSuccess(true);
                failedResult.setReassigned(true);

                Participant participant = participantMap.get(failedResult.getParticipantId());
                logger.info("🔄 重新分配: " + participant.getName() +
                        " 從 " + failedResult.getOriginalSeatId() +
                        " 重新分配到 " + newSeatId);
            }
        }

        // 10. 儲存抽籤結果
        logger.info("=== 保存抽籤結果 ===");
        logger.info("總結果數: " + results.size());

        int successCount = (int) results.stream().filter(LotteryResult::isSuccess).count();
        int failCount = results.size() - successCount;
        int reassignCount = (int) results.stream().filter(LotteryResult::isReassigned).count();

        logger.info("成功分配: " + successCount + ", 未分配: " + failCount + ", 重新分配: " + reassignCount);

        lotteryResultDao.createBatchResults(results);

        // 11. 更新活動狀態
        activityDao.updateActivityStatus(activityId, "completed");
        logger.info("活動狀態已更新為 completed");

        logger.info("=== 抽籤完成 ===");
        return true;
    }

    /**
     * 創建抽籤結果對象
     */
    private LotteryResult createLotteryResult(int activityId, Participant participant,
                                              String originalSeatId, String assignedSeatId,
                                              boolean success, int conflictCount,
                                              boolean reassigned, Date lotteryTime) {
        LotteryResult result = new LotteryResult();
        result.setActivityId(activityId);
        result.setParticipantId(participant.getId());
        result.setParticipantName(participant.getName());
        result.setOriginalSeatId(originalSeatId);
        result.setAssignedSeatId(assignedSeatId);
        result.setSeatId(assignedSeatId); // 為了向後兼容
        result.setSuccess(success);
        result.setConflictCount(conflictCount);
        result.setReassigned(reassigned);
        result.setStatus(success ? "success" : "failed");
        result.setLotteryTime(lotteryTime);
        return result;
    }

    /**
     * 座位可用性驗證
     */
    public Map<String, Object> validateSeatsAvailability(int activityId) throws SQLException {
        List<Seat> allSeats = seatDao.getSeatsByActivityId(activityId);
        List<Selection> selections = selectionDao.getSelectionsByActivityId(activityId);

        Set<String> availableSeatIds = allSeats.stream()
                .filter(Seat::isAvailable)
                .map(Seat::getSeatId)
                .collect(Collectors.toSet());

        List<String> invalidSelections = new ArrayList<>();
        for (Selection selection : selections) {
            if (!availableSeatIds.contains(selection.getSeatId())) {
                invalidSelections.add("用戶ID:" + selection.getParticipantId() +
                        " -> 座位:" + selection.getSeatId() + " (不可用)");
            }
        }

        Map<String, Object> validation = new HashMap<>();
        validation.put("totalSeats", allSeats.size());
        validation.put("availableSeats", availableSeatIds.size());
        validation.put("totalSelections", selections.size());
        validation.put("invalidSelections", invalidSelections);
        validation.put("hasInvalidSelections", !invalidSelections.isEmpty());

        return validation;
    }

    /**
     * 獲取抽籤結果
     */
    public Map<String, Object> getLotteryResult(int activityId) throws SQLException {
        List<LotteryResult> results = lotteryResultDao.getResultsByActivityId(activityId);
        if (results.isEmpty()) {
            return null;
        }

        // 獲取所有座位
        List<Seat> seats = seatDao.getSeatsByActivityId(activityId);
        Map<String, String> seatLabels = new HashMap<>();
        for (Seat seat : seats) {
            seatLabels.put(seat.getSeatId(), seat.getLabel());
        }

        // 獲取參與者信息
        List<Participant> participants = participantDao.getParticipantsByActivityId(activityId);
        Map<Integer, String> participantNames = new HashMap<>();
        for (Participant p : participants) {
            participantNames.put(p.getId(), p.getName());
        }

        // 統計資訊
        int totalParticipants = participants.size();
        int totalAvailableSeats = (int) seats.stream().filter(Seat::isAvailable).count();
        int assignedSeats = (int) results.stream().filter(LotteryResult::isSuccess).count();
        int reassignedCount = (int) results.stream().filter(LotteryResult::isReassigned).count();
        int conflictCount = (int) results.stream()
                .filter(r -> r.getConflictCount() > 0)
                .count();

        // 詳細結果
        List<Map<String, Object>> detailedResults = new ArrayList<>();
        for (LotteryResult result : results) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("participantId", result.getParticipantId());
            detail.put("participantName", participantNames.get(result.getParticipantId()));
            detail.put("originalSeat", result.getOriginalSeatId() != null ?
                    seatLabels.getOrDefault(result.getOriginalSeatId(), result.getOriginalSeatId()) : null);
            detail.put("assignedSeat", result.getAssignedSeatId() != null ?
                    seatLabels.getOrDefault(result.getAssignedSeatId(), result.getAssignedSeatId()) : null);
            detail.put("success", result.isSuccess());
            detail.put("reassigned", result.isReassigned());
            detail.put("conflictCount", result.getConflictCount());
            detail.put("lotteryTime", result.getLotteryTime());

            // 狀態描述
            String statusDescription;
            if (!result.isSuccess()) {
                if (result.getOriginalSeatId() == null) {
                    statusDescription = "未選座";
                } else {
                    statusDescription = "競爭失敗";
                }
            } else if (result.isReassigned()) {
                statusDescription = "重新分配";
            } else if (result.getConflictCount() > 0) {
                statusDescription = "競爭獲勝";
            } else {
                statusDescription = "直接分配";
            }
            detail.put("statusDescription", statusDescription);

            detailedResults.add(detail);
        }

        // 最終分配結果
        List<Map<String, Object>> finalAssignments = results.stream()
                .filter(LotteryResult::isSuccess)
                .map(result -> {
                    Map<String, Object> assignment = new HashMap<>();
                    assignment.put("participantId", result.getParticipantId());
                    assignment.put("participantName", participantNames.get(result.getParticipantId()));
                    assignment.put("seatId", result.getAssignedSeatId());
                    assignment.put("seatLabel", seatLabels.getOrDefault(result.getAssignedSeatId(), result.getAssignedSeatId()));
                    assignment.put("reassigned", result.isReassigned());
                    return assignment;
                })
                .collect(Collectors.toList());

        // 組裝返回結果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("finalAssignments", finalAssignments);
        resultData.put("statistics", Map.of(
                "totalParticipants", totalParticipants,
                "totalAvailableSeats", totalAvailableSeats,
                "assignedSeats", assignedSeats,
                "reassignedCount", reassignedCount,
                "conflictCount", conflictCount,
                "unassignedCount", totalParticipants - assignedSeats
        ));
        resultData.put("detailedResults", detailedResults);
        resultData.put("lotteryTime", results.get(0).getLotteryTime());

        return resultData;
    }
}