package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDao {
    

    public List<Seat> getSeatsByActivityId(int activityId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE activity_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seats.add(seat);
                }
            }
        }

        return seats;
    }

    public Seat getSeatBySeatId(int activityId, String seatId) throws SQLException {
        String sql = "SELECT * FROM seats WHERE activity_id = ? AND seat_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setString(2, seatId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeat(rs);
                }
            }
        }

        return null;
    }

    public boolean createSeat(Seat seat) throws SQLException {
        // 使用反引號(``)來轉義 MySQL 保留關鍵字 row 和 col
        String sql = "INSERT INTO seats (activity_id, seat_id, `row`, `col`, label, available) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, seat.getActivityId());
            stmt.setString(2, seat.getSeatId());
            stmt.setInt(3, seat.getRow());
            stmt.setInt(4, seat.getCol());
            stmt.setString(5, seat.getLabel());
            stmt.setBoolean(6, seat.isAvailable());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seat.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean updateSeatAvailability(int activityId, String seatId, boolean available) throws SQLException {
        String sql = "UPDATE seats SET available = ? WHERE activity_id = ? AND seat_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, available);
            stmt.setInt(2, activityId);
            stmt.setString(3, seatId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteSeatsByActivityId(int activityId) throws SQLException {
        String sql = "DELETE FROM seats WHERE activity_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);

            stmt.executeUpdate();
            return true;
        }
    }

    public int createBatchSeats(List<Seat> seats) throws SQLException {
        // 使用反引號(``)來轉義 MySQL 保留關鍵字 row 和 col
        String sql = "INSERT INTO seats (activity_id, seat_id, `row`, `col`, label, available) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        int insertedCount = 0;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Seat seat : seats) {
                stmt.setInt(1, seat.getActivityId());
                stmt.setString(2, seat.getSeatId());
                stmt.setInt(3, seat.getRow());
                stmt.setInt(4, seat.getCol());
                stmt.setString(5, seat.getLabel());
                stmt.setBoolean(6, seat.isAvailable());
                stmt.addBatch();
                insertedCount++;
            }

            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw e;
        }

        return insertedCount;
    }

    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setId(rs.getInt("id"));
        seat.setActivityId(rs.getInt("activity_id"));
        seat.setSeatId(rs.getString("seat_id"));

        // 不要在 ResultSet 中使用反引號
        seat.setRow(rs.getInt("row"));  // 移除反引號
        seat.setCol(rs.getInt("col"));  // 移除反引號

        seat.setLabel(rs.getString("label"));
        seat.setAvailable(rs.getBoolean("available"));
        return seat;
    }



}