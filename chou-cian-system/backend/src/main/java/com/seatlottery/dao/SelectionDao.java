package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.Selection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SelectionDao {

    public List<Selection> getSelectionsByActivityId(int activityId) throws SQLException {
        List<Selection> selections = new ArrayList<>();
        String sql = "SELECT s.*, p.name as user_name " +
                "FROM selections s " +
                "JOIN participants p ON s.participant_id = p.id " +
                "WHERE s.activity_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Selection selection = mapResultSetToSelection(rs);
                    selection.setUserName(rs.getString("user_name"));
                    selections.add(selection);
                }
            }
        }

        return selections;
    }

    public Selection getSelectionByParticipantId(int activityId, int participantId) throws SQLException {
        String sql = "SELECT s.*, p.name as user_name " +
                "FROM selections s " +
                "JOIN participants p ON s.participant_id = p.id " +
                "WHERE s.activity_id = ? AND s.participant_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setInt(2, participantId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Selection selection = mapResultSetToSelection(rs);
                    selection.setUserName(rs.getString("user_name"));
                    return selection;
                }
            }
        }

        return null;
    }

    public List<Selection> getSelectionsBySeatId(int activityId, String seatId) throws SQLException {
        List<Selection> selections = new ArrayList<>();
        String sql = "SELECT s.*, p.name as user_name " +
                "FROM selections s " +
                "JOIN participants p ON s.participant_id = p.id " +
                "WHERE s.activity_id = ? AND s.seat_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setString(2, seatId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Selection selection = mapResultSetToSelection(rs);
                    selection.setUserName(rs.getString("user_name"));
                    selections.add(selection);
                }
            }
        }

        return selections;
    }

    public Selection createSelection(Selection selection) throws SQLException {
        String sql = "INSERT INTO selections (activity_id, participant_id, seat_id, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, selection.getActivityId());
            stmt.setInt(2, selection.getParticipantId());
            stmt.setString(3, selection.getSeatId());
            stmt.setString(4, selection.getStatus() != null ? selection.getStatus() : "confirmed");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("創建選座記錄失敗，沒有影響到任何行");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    selection.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("創建選座記錄失敗，沒有獲取到ID");
                }
            }

            // Get the automatically generated selection_time
            String getTimeSql = "SELECT selection_time FROM selections WHERE id = ?";
            try (PreparedStatement timeStmt = conn.prepareStatement(getTimeSql)) {
                timeStmt.setInt(1, selection.getId());
                try (ResultSet rs = timeStmt.executeQuery()) {
                    if (rs.next()) {
                        selection.setSelectionTime(rs.getTimestamp("selection_time"));
                    }
                }
            }
        }

        return selection;
    }

    public boolean updateSelection(Selection selection) throws SQLException {
        String sql = "UPDATE selections SET seat_id = ?, status = ? " +
                "WHERE activity_id = ? AND participant_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selection.getSeatId());
            stmt.setString(2, selection.getStatus());
            stmt.setInt(3, selection.getActivityId());
            stmt.setInt(4, selection.getParticipantId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteSelection(int activityId, int participantId) throws SQLException {
        String sql = "DELETE FROM selections WHERE activity_id = ? AND participant_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setInt(2, participantId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Selection mapResultSetToSelection(ResultSet rs) throws SQLException {
        Selection selection = new Selection();
        selection.setId(rs.getInt("id"));
        selection.setActivityId(rs.getInt("activity_id"));
        selection.setParticipantId(rs.getInt("participant_id"));
        selection.setSeatId(rs.getString("seat_id"));
        selection.setSelectionTime(rs.getTimestamp("selection_time"));
        selection.setStatus(rs.getString("status"));
        return selection;
    }
}