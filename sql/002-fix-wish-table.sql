-- ============================================================
-- 流星树洞 · 织星海 — 修复 wish 表缺失列 & 补齐表结构
-- ============================================================
-- 使用方式: mysql -u root -p star_weave < sql/002-fix-wish-table.sql
-- ============================================================

USE star_weave;

-- ============================================================
-- 1. wish 表：补 status / review_reason / reviewed_at 列
-- ============================================================

-- 先检查并补 status 列
SET @has_status = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'status'
);
SET @sql = IF(@has_status = 0,
  'ALTER TABLE `wish` ADD COLUMN `status` ENUM(''pending'',''approved'',''rejected'') NOT NULL DEFAULT ''approved'' COMMENT ''AI审核状态'' AFTER `content`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 补 review_reason 列
SET @has_reason = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'review_reason'
);
SET @sql = IF(@has_reason = 0,
  'ALTER TABLE `wish` ADD COLUMN `review_reason` VARCHAR(255) DEFAULT NULL COMMENT ''审核拒绝原因'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 补 reviewed_at 列
SET @has_reviewed = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'reviewed_at'
);
SET @sql = IF(@has_reviewed = 0,
  'ALTER TABLE `wish` ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL COMMENT ''审核时间'' AFTER `review_reason`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. message 表：补 heal_tag / healing_message / wish_count 列
-- ============================================================

SET @has_heal_tag = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'heal_tag'
);
SET @sql = IF(@has_heal_tag = 0,
  'ALTER TABLE `message` ADD COLUMN `heal_tag` VARCHAR(8) DEFAULT NULL COMMENT ''治愈标签（2-4字）'' AFTER `review_reason`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_healing = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'healing_message'
);
SET @sql = IF(@has_healing = 0,
  'ALTER TABLE `message` ADD COLUMN `healing_message` TEXT DEFAULT NULL COMMENT ''AI治愈回复'' AFTER `heal_tag`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_wish_count = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'wish_count'
);
SET @sql = IF(@has_wish_count = 0,
  'ALTER TABLE `message` ADD COLUMN `wish_count` INT NOT NULL DEFAULT 0 COMMENT ''许愿次数'' AFTER `healing_message`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. catch_history 表：不存在则创建
-- ============================================================

CREATE TABLE IF NOT EXISTS `catch_history` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`    BIGINT        NOT NULL                 COMMENT '捞取者用户ID',
  `meteor_id`  BIGINT        NOT NULL                 COMMENT '被捞起的流星ID',
  `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捞取时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_meteor` (`meteor_id`),
  CONSTRAINT `fk_catch_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_catch_message` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捞取历史表';

-- ============================================================
-- 4. user 表：补 bio / agreed_policy / agreed_at 列
-- ============================================================

SET @has_bio = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'bio'
);
SET @sql = IF(@has_bio = 0,
  'ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(200) DEFAULT '''' COMMENT ''个人签名'' AFTER `is_admin`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_agreed_policy = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'agreed_policy'
);
SET @sql = IF(@has_agreed_policy = 0,
  'ALTER TABLE `user` ADD COLUMN `agreed_policy` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否同意用户协议和隐私政策'' AFTER `bio`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_agreed_at = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'agreed_at'
);
SET @sql = IF(@has_agreed_at = 0,
  'ALTER TABLE `user` ADD COLUMN `agreed_at` DATETIME DEFAULT NULL COMMENT ''同意协议时间'' AFTER `agreed_policy`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 完成
-- ============================================================
SELECT '数据库修复完成 ✅' AS result;
