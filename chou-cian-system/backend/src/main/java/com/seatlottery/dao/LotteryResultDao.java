package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.LotteryResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class LotteryResultDao {
    Logger logger = Logger.getLogger("testLogger");

    public List<LotteryResult> getResultsByActivityId(int activityId) throws SQLException {
        // ⭐ 修正查詢欄位名稱
        String sql = "SELECT id, activity_id, participant_id, participant_name, " +
                "original_seat_id, assigned_seat_id, is_success, reassigned, " +
                "conflict_count, status, lottery_time " +
                "FROM lottery_results WHERE activity_id = ? ORDER BY lottery_time";

        List<LotteryResult> results = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LotteryResult result = new LotteryResult();
                    result.setId(rs.getInt("id"));
                    result.setActivityId(rs.getInt("activity_id"));
                    result.setParticipantId(rs.getInt("participant_id"));
                    result.setParticipantName(rs.getString("participant_name"));
                    result.setOriginalSeatId(rs.getString("original_seat_id"));
                    result.setAssignedSeatId(rs.getString("assigned_seat_id"));
                    result.setSuccess(rs.getBoolean("is_success")); // ⭐ 重要
                    result.setReassigned(rs.getBoolean("reassigned"));
                    result.setConflictCount(rs.getInt("conflict_count"));
                    result.setStatus(rs.getString("status"));
                    result.setLotteryTime(rs.getTimestamp("lottery_time"));

                    // 為了向後兼容，設置 seatId
                    result.setSeatId(result.getAssignedSeatId());

                    results.add(result);
                }
            }
        }

        return results;
    }

    public boolean createLotteryResult(LotteryResult result) throws SQLException {
        // ⭐ 修正欄位名稱：success -> is_success
        String sql = "INSERT INTO lottery_results " +
                "(activity_id, participant_id, participant_name, original_seat_id, " +
                "assigned_seat_id, is_success, reassigned, conflict_count, status, lottery_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, result.getActivityId());
            stmt.setInt(2, result.getParticipantId());
            stmt.setString(3, result.getParticipantName());
            stmt.setString(4, result.getOriginalSeatId());
            stmt.setString(5, result.getAssignedSeatId());
            stmt.setBoolean(6, result.isSuccess()); // 這裡對應 is_success
            stmt.setBoolean(7, result.isReassigned());
            stmt.setInt(8, result.getConflictCount());
            stmt.setString(9, result.getStatus());
            stmt.setTimestamp(10, new Timestamp(result.getLotteryTime().getTime()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public void clearLotteryResults(int activityId) throws SQLException {
        String sql = "DELETE FROM lottery_results WHERE activity_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            int deletedRows = stmt.executeUpdate();

            System.out.println("已清除 " + deletedRows + " 條舊的抽籤結果");
        }
    }

    public void createBatchResults(List<LotteryResult> results) throws SQLException {
        for (LotteryResult result : results) {
            createLotteryResult(result);
        }
    }

    private LotteryResult mapResultSetToLotteryResult(ResultSet rs) throws SQLException {
        LotteryResult result = new LotteryResult();
        result.setId(rs.getInt("id"));
        result.setActivityId(rs.getInt("activity_id"));
        result.setParticipantId(rs.getInt("participant_id"));
        result.setSeatId(rs.getString("seat_id"));
        result.setSuccess(rs.getBoolean("is_success"));
        result.setConflictCount(rs.getInt("conflict_count"));
        result.setLotteryTime(rs.getTimestamp("lottery_time"));
        return result;
    }

    public void deleteResultsByActivityId(int activityId) throws SQLException {
        clearLotteryResults(activityId); // 重用清除方法
    }
}