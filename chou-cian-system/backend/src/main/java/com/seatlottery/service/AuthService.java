package com.seatlottery.service;

import com.seatlottery.dao.AdminDao;
import com.seatlottery.model.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;

public class AuthService {
    private AdminDao adminDao;
    private final SecretKey key;
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小時

    public AuthService() {
        this.adminDao = new AdminDao();
        // 在實際應用中，應該使用環境變量或配置文件中的密鑰
        String secretKey = "seatLotterySecretKey123456789012345678901234567890";
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String login(String username, String password) throws SQLException {
        Admin admin = adminDao.getAdminByUsername(username);
        if (admin == null) {
            return null;
        }

        // 驗證密碼
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(admin.getPassword())) {
            return null;
        }

        // 更新最後登入時間
        adminDao.updateLastLoginTime(admin.getId());

        // 生成 JWT token
        return generateToken(admin);
    }

    public Admin validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            return adminDao.getAdminByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean changePassword(int adminId, String oldPassword, String newPassword) throws SQLException {
        Admin admin = adminDao.getAdminById(adminId);
        if (admin == null) {
            return false;
        }

        String hashedOldPassword = hashPassword(oldPassword);
        if (!hashedOldPassword.equals(admin.getPassword())) {
            return false;
        }

        String hashedNewPassword = hashPassword(newPassword);
        return adminDao.changePassword(adminId, hashedNewPassword);
    }

    private String generateToken(Admin admin) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(admin.getUsername())
                .claim("name", admin.getName())
                .claim("role", admin.getRole())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 不可用", e);
        }
    }
}