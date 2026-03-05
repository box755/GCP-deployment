package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantDao {

    public List<Participant> getParticipantsByActivityId(int activityId) throws SQLException {
        List<Participant> participants = new ArrayList<>();
        String sql = "SELECT * FROM participants WHERE activity_id = ? ORDER BY join_time";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Participant participant = mapResultSetToParticipant(rs);
                    participants.add(participant);
                }
            }
        }

        return participants;
    }

    public Participant getParticipantById(int id) throws SQLException {
        String sql = "SELECT * FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                }
            }
        }

        return null;
    }

    public Participant createParticipant(Participant participant) throws SQLException {
        String sql = "INSERT INTO participants (activity_id, name, contact) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, participant.getActivityId());
            stmt.setString(2, participant.getName());
            stmt.setString(3, participant.getContact());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("創建參與者失敗，沒有影響到任何行");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participant.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("創建參與者失敗，沒有獲取到ID");
                }
            }

            // Get the automatically generated join_time
            String getTimeSql = "SELECT join_time FROM participants WHERE id = ?";
            try (PreparedStatement timeStmt = conn.prepareStatement(getTimeSql)) {
                timeStmt.setInt(1, participant.getId());
                try (ResultSet rs = timeStmt.executeQuery()) {
                    if (rs.next()) {
                        participant.setJoinTime(rs.getTimestamp("join_time"));
                    }
                }
            }
        }

        return participant;
    }

    public boolean deleteParticipant(int id) throws SQLException {
        String sql = "DELETE FROM participants WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Participant mapResultSetToParticipant(ResultSet rs) throws SQLException {
        Participant participant = new Participant();
        participant.setId(rs.getInt("id"));
        participant.setActivityId(rs.getInt("activity_id"));
        participant.setName(rs.getString("name"));
        participant.setContact(rs.getString("contact"));
        participant.setJoinTime(rs.getTimestamp("join_time"));
        return participant;
    }



}