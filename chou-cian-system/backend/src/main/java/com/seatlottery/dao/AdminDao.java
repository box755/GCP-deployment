package com.seatlottery.dao;

import com.seatlottery.config.DatabaseConfig;
import com.seatlottery.model.Admin;

import java.sql.*;

public class AdminDao {

    public Admin getAdminByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdmin(rs);
                }
            }
        }

        return null;
    }

    public Admin getAdminById(int id) throws SQLException {
        String sql = "SELECT * FROM admins WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdmin(rs);
                }
            }
        }

        return null;
    }

    public boolean updateLastLoginTime(int adminId) throws SQLException {
        String sql = "UPDATE admins SET last_login = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean changePassword(int adminId, String newPassword) throws SQLException {
        String sql = "UPDATE admins SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setName(rs.getString("name"));
        admin.setRole(rs.getString("role"));
        admin.setCreateTime(rs.getTimestamp("create_time"));
        admin.setLastLogin(rs.getTimestamp("last_login"));
        return admin;
    }
}