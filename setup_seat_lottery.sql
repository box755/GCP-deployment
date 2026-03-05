-- 建立所有資料表 (從 Java DAO 反推)

USE seat_lottery;

-- 1. admins 表
CREATE TABLE IF NOT EXISTS `admins` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(128) NOT NULL, -- 存放 SHA-512 Hash
    `name` VARCHAR(50) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'admin',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `last_login` TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. activities 表
CREATE TABLE IF NOT EXISTS `activities` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `selection_start_time` TIMESTAMP NOT NULL,
    `selection_end_time` TIMESTAMP NOT NULL,
    `rows` INT NOT NULL,
    `cols` INT NOT NULL,
    `total_seats` INT NOT NULL,
    `participant_count` INT DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'pending',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. seats 表
CREATE TABLE IF NOT EXISTS `seats` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL,
    `seat_id` VARCHAR(20) NOT NULL,
    `row` INT NOT NULL,
    `col` INT NOT NULL,
    `label` VARCHAR(50) NOT NULL,
    `available` BOOLEAN DEFAULT TRUE,
    UNIQUE KEY `uk_activity_seat` (`activity_id`, `seat_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. participants 表
CREATE TABLE IF NOT EXISTS `participants` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `contact` VARCHAR(100) NOT NULL,
    `join_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. selections 表
CREATE TABLE IF NOT EXISTS `selections` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL,
    `participant_id` INT NOT NULL,
    `seat_id` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'confirmed',
    `selection_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_activity_participant` (`activity_id`, `participant_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`participant_id`) REFERENCES `participants`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. lottery_results 表
CREATE TABLE IF NOT EXISTS `lottery_results` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL,
    `participant_id` INT NOT NULL,
    `participant_name` VARCHAR(50) NOT NULL,
    `original_seat_id` VARCHAR(20),
    `assigned_seat_id` VARCHAR(20),
    `is_success` BOOLEAN DEFAULT FALSE,
    `reassigned` BOOLEAN DEFAULT FALSE,
    `conflict_count` INT DEFAULT 0,
    `status` VARCHAR(20),
    `lottery_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`participant_id`) REFERENCES `participants`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 插入初始管理員資料
-- ==========================================

-- 密碼為 'admin123' 的 SHA-512 Hash
INSERT IGNORE INTO `admins` (`username`, `password`, `name`, `role`)
VALUES ('admin', '7fcf4ba391c48784edde599889d6e3f1e47a27db36ecc050cc92f259bfac38afad2c68a1ae804d77075e8fb722503f3eca2b2c1006ee6f6c7b7628cb45fffd1d', '系統管理員', 'super_admin');
