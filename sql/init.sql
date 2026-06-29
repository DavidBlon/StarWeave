-- ============================================================
-- 流星树洞 · 织星海 — 完整建库 & 升级脚本
-- ============================================================
-- 使用方式:
--   新建库: mysql -u root -p < sql/init.sql
--   已有库: mysql -u root -p star_weave < sql/init.sql
-- ============================================================
-- 管理员凭据: admin / admin888
-- 管理员也可由 Java AdminInitializer 在启动时自动创建
-- ============================================================

CREATE DATABASE IF NOT EXISTS star_weave
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE star_weave;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
  `username`      VARCHAR(50)   NOT NULL                 COMMENT '登录用户名（唯一，不可修改）',
  `nickname`      VARCHAR(50)   NOT NULL                 COMMENT '显示昵称',
  `avatar_url`    TEXT          DEFAULT NULL             COMMENT '头像地址（base64 或 emoji 标识）',
  `password_hash` VARCHAR(255)  DEFAULT NULL             COMMENT '密码哈希（BCrypt）',
  `bio`           VARCHAR(200)  DEFAULT ''               COMMENT '个人签名',
  `border_style`  VARCHAR(50)   DEFAULT 'default'        COMMENT '星图边框样式（赞助权益）',
  `is_sponsor`    TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否为赞助者',
  `is_admin`      TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否为管理员',
  `token_version` INT           NOT NULL DEFAULT 0       COMMENT '登录 token 版本号（每次登录递增，用于单设备登录校验）',
  `agreed_policy` TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否同意用户协议和隐私政策',
  `agreed_at`     DATETIME      DEFAULT NULL             COMMENT '同意协议时间',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`),
  INDEX `idx_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 流星消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS `message` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  `user_id`         BIGINT        NOT NULL                 COMMENT '发布者用户ID',
  `content`         TEXT          NOT NULL                 COMMENT '消息内容',
  `color`           VARCHAR(20)   DEFAULT '#FFD700'        COMMENT '流星颜色',
  `status`          ENUM('pending','approved','rejected')
                    NOT NULL DEFAULT 'pending'             COMMENT 'AI审核状态',
  `review_reason`   VARCHAR(255)  DEFAULT NULL             COMMENT '审核拒绝原因',
  `heal_tag`        VARCHAR(50)   DEFAULT NULL             COMMENT 'AI 治愈标签',
  `healing_message` TEXT          DEFAULT NULL             COMMENT 'AI 治愈回信',
  `wish_count`      INT           NOT NULL DEFAULT 0       COMMENT '回复/许愿数',
  `is_caught`       TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否已被捞起',
  `caught_by`       BIGINT        DEFAULT NULL             COMMENT '捞起者用户ID',
  `caught_at`       DATETIME      DEFAULT NULL             COMMENT '捞起时间',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_caught` (`is_caught`, `caught_by`),
  INDEX `idx_created` (`created_at`),
  CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流星消息表';

-- ============================================================
-- 3. 回复/许愿表
-- ============================================================
CREATE TABLE IF NOT EXISTS `wish` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '回复ID',
  `meteor_id`     BIGINT        NOT NULL                 COMMENT '关联流星ID',
  `user_id`       BIGINT        NOT NULL                 COMMENT '回复者用户ID',
  `content`       TEXT          NOT NULL                 COMMENT '回复内容',
  `status`        ENUM('pending','approved','rejected')
                  NOT NULL DEFAULT 'pending'             COMMENT '审核状态',
  `review_reason` VARCHAR(255)  DEFAULT NULL             COMMENT '审核拒绝原因',
  `reviewed_at`   DATETIME      DEFAULT NULL             COMMENT '审核时间',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meteor` (`meteor_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_wish_meteor` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wish_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复/许愿表';

-- ============================================================
-- 4. 捞取历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS `catch_history` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
  `user_id`       BIGINT        NOT NULL                 COMMENT '捞取者用户ID',
  `meteor_id`     BIGINT        NOT NULL                 COMMENT '被捞取的流星ID',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捞取时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_meteor` (`meteor_id`),
  CONSTRAINT `fk_catch_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_catch_meteor` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捞取历史表';

-- ============================================================
-- 5. 星图表
-- ============================================================
CREATE TABLE IF NOT EXISTS `star_map` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '星图ID',
  `message_id`    BIGINT        NOT NULL                 COMMENT '关联消息ID',
  `user_id`       BIGINT        NOT NULL                 COMMENT '生成者ID',
  `content_hash`  VARCHAR(64)   NOT NULL                 COMMENT '内容SHA256哈希（确定性种子）',
  `image_url`     VARCHAR(255)  DEFAULT NULL             COMMENT '渲染图地址',
  `image_hd_url`  VARCHAR(255)  DEFAULT NULL             COMMENT '高清无码图地址（付费）',
  `is_premium`    TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否已付费解锁高清',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  PRIMARY KEY (`id`),
  INDEX `idx_message` (`message_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_hash` (`content_hash`),
  CONSTRAINT `fk_starmap_message` FOREIGN KEY (`message_id`) REFERENCES `message`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_starmap_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='星图表';

-- ============================================================
-- 6. 星光守护者表（赞助记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS `sponsor` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '赞助记录ID',
  `user_id`       BIGINT        DEFAULT NULL             COMMENT '关联用户ID（可为空=匿名赞助）',
  `display_name`  VARCHAR(50)   NOT NULL                 COMMENT '展示名称',
  `message`       VARCHAR(500)  DEFAULT NULL             COMMENT '守护寄语',
  `border_style`  VARCHAR(50)   DEFAULT 'sponsor'        COMMENT '赞助边框样式',
  `amount`        DECIMAL(10,2) NOT NULL DEFAULT 0.00    COMMENT '赞助金额',
  `platform`      VARCHAR(30)   DEFAULT 'afdian'         COMMENT '赞助平台（afdian/mcdonal/wechat）',
  `is_active`     TINYINT(1)    NOT NULL DEFAULT 1       COMMENT '是否在展示列表',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '赞助时间',
  PRIMARY KEY (`id`),
  INDEX `idx_active` (`is_active`, `created_at`),
  INDEX `idx_user` (`user_id`),
  CONSTRAINT `fk_sponsor_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赞助记录（星光守护者）';

-- ============================================================
-- 7. AI 审核日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_review_log` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `message_id`    BIGINT        NOT NULL                 COMMENT '审核的消息ID',
  `result`        ENUM('approved','rejected')
                  NOT NULL                               COMMENT '审核结果',
  `confidence`    DECIMAL(5,4)  NOT NULL DEFAULT 0.0000  COMMENT '置信度（0-1）',
  `reason`        VARCHAR(500)  DEFAULT NULL             COMMENT '审核理由',
  `reviewed_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  INDEX `idx_message` (`message_id`),
  INDEX `idx_result` (`result`),
  CONSTRAINT `fk_review_message` FOREIGN KEY (`message_id`) REFERENCES `message`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI审核日志表';

-- ============================================================
-- 8. 爱发电账号绑定表
-- ============================================================
CREATE TABLE IF NOT EXISTS `afdian_account_link` (
  `id`                     BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '绑定ID',
  `user_id`                BIGINT        NOT NULL                 COMMENT '本站用户ID',
  `afdian_user_id`         VARCHAR(100)  NOT NULL                 COMMENT '爱发电用户ID',
  `afdian_user_private_id` VARCHAR(100)  DEFAULT NULL             COMMENT '爱发电用户私有ID',
  `created_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_afdian_user_id` (`afdian_user_id`),
  KEY `idx_afdian_user_private_id` (`afdian_user_private_id`),
  CONSTRAINT `fk_afdian_link_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爱发电账号绑定表';

-- ============================================================
-- 9. 初始数据：星光守护者样例
-- ============================================================
INSERT IGNORE INTO `sponsor` (`display_name`, `message`, `border_style`, `amount`, `platform`, `is_active`) VALUES
  ('星野',   '愿每一颗流星都找到归处',                                 'sponsor', 66.00, 'afdian', 1),
  ('夜航船', '在黑暗中为你点亮一束光',                                 'sponsor', 33.00, 'afdian', 1),
  ('阿九',   '星星发亮是为了让每一个人有一天都能找到属于自己的星星', 'sponsor', 99.00, 'afdian', 1),
  ('鹿鸣',   '野鹿鸣啾啾，星河入梦来',                               'sponsor', 18.00, 'afdian', 1),
  ('白鲸',   '深海有鲸，夜空有星',                                   'sponsor', 52.00, 'afdian', 1);

-- ============================================================
-- 10. 初始数据：管理员（密码: admin888，BCrypt 哈希）
--     注意：AdminInitializer 在启动时也会自动创建/迁移
-- ============================================================
INSERT IGNORE INTO `user` (`username`, `nickname`, `password_hash`, `bio`, `border_style`, `is_sponsor`, `is_admin`, `token_version`)
VALUES ('admin', '管理员', '$2a$10$g85qJhZKVRN5K9SVy7SzFuVUIug6xTB3jJzZCx6XcXsrPFPhhARx6',
        '✦ 星海管理者', 'admin', 0, 1, 0);

-- ============================================================
-- 11. 迁移：为已有数据库补全缺失字段
--     使用 INFORMATION_SCHEMA 检查，可重复安全执行
-- ============================================================

-- ── user 表 ──
SET @has_bio = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'bio');
SET @sql = IF(@has_bio = 0,
  'ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(200) DEFAULT '''' COMMENT ''个人签名'' AFTER `avatar_url`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_username = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'username');
SET @sql = IF(@has_username = 0,
  'ALTER TABLE `user` ADD COLUMN `username` VARCHAR(50) NOT NULL COMMENT ''登录用户名（唯一，不可修改）'' AFTER `id`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_admin = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'is_admin');
SET @sql = IF(@has_admin = 0,
  'ALTER TABLE `user` ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否为管理员'' AFTER `is_sponsor`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_token_ver = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'token_version');
SET @sql = IF(@has_token_ver = 0,
  'ALTER TABLE `user` ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT ''登录 token 版本号'' AFTER `is_admin`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_policy = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'agreed_policy');
SET @sql = IF(@has_policy = 0,
  'ALTER TABLE `user` ADD COLUMN `agreed_policy` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否同意用户协议'' AFTER `token_version`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_agreed_at = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'agreed_at');
SET @sql = IF(@has_agreed_at = 0,
  'ALTER TABLE `user` ADD COLUMN `agreed_at` DATETIME DEFAULT NULL COMMENT ''同意协议时间'' AFTER `agreed_policy`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ── message 表 ──
SET @has_heal_tag = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'heal_tag');
SET @sql = IF(@has_heal_tag = 0,
  'ALTER TABLE `message` ADD COLUMN `heal_tag` VARCHAR(8) DEFAULT NULL COMMENT ''治愈标签（2-4字）'' AFTER `review_reason`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_healing = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'healing_message');
SET @sql = IF(@has_healing = 0,
  'ALTER TABLE `message` ADD COLUMN `healing_message` TEXT DEFAULT NULL COMMENT ''AI治愈回复'' AFTER `heal_tag`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_wish_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'message' AND COLUMN_NAME = 'wish_count');
SET @sql = IF(@has_wish_count = 0,
  'ALTER TABLE `message` ADD COLUMN `wish_count` INT NOT NULL DEFAULT 0 COMMENT ''许愿次数'' AFTER `healing_message`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ── wish 表 ──
SET @has_status = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'status');
SET @sql = IF(@has_status = 0,
  'ALTER TABLE `wish` ADD COLUMN `status` ENUM(''pending'',''approved'',''rejected'') NOT NULL DEFAULT ''approved'' COMMENT ''AI审核状态'' AFTER `content`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_reason = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'review_reason');
SET @sql = IF(@has_reason = 0,
  'ALTER TABLE `wish` ADD COLUMN `review_reason` VARCHAR(255) DEFAULT NULL COMMENT ''审核拒绝原因'' AFTER `status`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_reviewed = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'star_weave' AND TABLE_NAME = 'wish' AND COLUMN_NAME = 'reviewed_at');
SET @sql = IF(@has_reviewed = 0,
  'ALTER TABLE `wish` ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL COMMENT ''审核时间'' AFTER `review_reason`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 12. 修复：将历史遗留的 NULL token_version 设为 0
--     （旧版 AdminInitializer / 注册未设默认值导致）
-- ============================================================
UPDATE `user` SET `token_version` = 0 WHERE `token_version` IS NULL;

-- ============================================================
-- 完成
-- ============================================================
SELECT '数据库初始化/升级完成 ✅' AS result;
