package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;


public class ActivityDao {

    public List<Activity> getAllActivities() throws SQLException {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities ORDER BY create_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Activity activity = mapResultSetToActivity(rs);
                activities.add(activity);
            }
        }

        return activities;
    }

    public Activity getActivityById(int id) throws SQLException {
        String sql = "SELECT * FROM activities WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToActivity(rs);
                }
            }
        }

        return null;
    }

    public Activity createActivity(Activity activity) throws SQLException {
        // 注意這裡使用反引號(``)來轉義 MySQL 保留關鍵字
        String sql = "INSERT INTO activities (name, description, selection_start_time, " +
                "selection_end_time, `rows`, `cols`, total_seats, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, activity.getName());
            stmt.setString(2, activity.getDescription());
            stmt.setTimestamp(3, new Timestamp(activity.getSelectionStartTime().getTime()));
            stmt.setTimestamp(4, new Timestamp(activity.getSelectionEndTime().getTime()));
            stmt.setInt(5, activity.getRows());
            stmt.setInt(6, activity.getCols());
            stmt.setInt(7, activity.getTotalSeats());
            stmt.setString(8, activity.getStatus() != null ? activity.getStatus() : "pending");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("創建活動失敗，沒有影響到任何行");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    activity.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("創建活動失敗，沒有獲取到ID");
                }
            }
        }

        return activity;
    }


    public boolean deleteActivity(int id) throws SQLException {
        String sql = "DELETE FROM activities WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean incrementParticipantCount(int activityId) throws SQLException {
        String sql = "UPDATE activities SET participant_count = participant_count + 1 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // 添加專門的狀態更新方法
    public boolean updateActivityStatus(int activityId, String status) throws SQLException {
        String sql = "UPDATE activities SET status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, activityId);

            int affectedRows = stmt.executeUpdate();

            // 添加調試日誌
            if (affectedRows > 0) {
                System.out.println("成功更新活動狀態: ID=" + activityId + ", 新狀態=" + status);
            } else {
                System.out.println("更新活動狀態失敗: 沒有影響到任何行, ID=" + activityId);
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("更新活動狀態時發生SQL錯誤: " + e.getMessage());
            throw e;
        }
    }

    // 如果您想要一個完整的活動更新方法
    public boolean updateActivity(Activity activity) throws SQLException {
        String sql = "UPDATE activities SET name = ?, description = ?, " +
                "selection_start_time = ?, selection_end_time = ?, " +
                "status = ?, update_time = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, activity.getName());
            stmt.setString(2, activity.getDescription());
            stmt.setTimestamp(3, new Timestamp(activity.getSelectionStartTime().getTime()));
            stmt.setTimestamp(4, new Timestamp(activity.getSelectionEndTime().getTime()));
            stmt.setString(5, activity.getStatus());
            stmt.setInt(6, activity.getId());

            int affectedRows = stmt.executeUpdate();

            // 添加調試日誌
            if (affectedRows > 0) {
                System.out.println("成功更新活動: ID=" + activity.getId() + ", 狀態=" + activity.getStatus());
            } else {
                System.out.println("更新活動失敗: 沒有影響到任何行, ID=" + activity.getId());
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("更新活動時發生SQL錯誤: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Activity mapResultSetToActivity(ResultSet rs) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getInt("id"));
        activity.setName(rs.getString("name"));
        activity.setDescription(rs.getString("description"));
        activity.setSelectionStartTime(rs.getTimestamp("selection_start_time"));
        activity.setSelectionEndTime(rs.getTimestamp("selection_end_time"));

        // 不要在 ResultSet 中使用反引號
        activity.setRows(rs.getInt("rows"));  // 移除反引號
        activity.setCols(rs.getInt("cols"));  // 移除反引號

        activity.setTotalSeats(rs.getInt("total_seats"));
        activity.setParticipantCount(rs.getInt("participant_count"));
        activity.setStatus(rs.getString("status"));
        activity.setCreateTime(rs.getTimestamp("create_time"));
        activity.setUpdateTime(rs.getTimestamp("update_time"));
        return activity;
    }

    /**
     * 恢復選位 - 更新狀態為 active 並延長結束時間
     */
    public boolean resumeSelection(int activityId, Date newEndTime) throws SQLException {
        String sql = "UPDATE activities SET status = ?, selection_end_time = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "active");
            stmt.setTimestamp(2, new Timestamp(newEndTime.getTime()));
            stmt.setInt(3, activityId);

            int affectedRows = stmt.executeUpdate();

            // 添加調試日誌
            if (affectedRows > 0) {
                System.out.println("成功恢復選位: ID=" + activityId + ", 新結束時間=" + newEndTime);
            } else {
                System.out.println("恢復選位失敗: 沒有影響到任何行, ID=" + activityId);
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("恢復選位時發生SQL錯誤: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 恢復選位 - 默認延長1小時
     */
    public boolean resumeSelection(int activityId) throws SQLException {
        // 計算新的結束時間：當前時間 + 1小時
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date newEndTime = cal.getTime();

        return resumeSelection(activityId, newEndTime);
    }

}