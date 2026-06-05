-- ============================================================
-- 流星树洞 · 织星海 — 新增流星特性（治愈标签、许愿、捞取记录）
-- ============================================================
-- 使用方式: mysql -u root -p star_weave < sql/001-add-meteor-features.sql
-- ============================================================

USE star_weave;

-- ============================================================
-- message 表新增字段
-- ============================================================
ALTER TABLE `message`
  ADD COLUMN `heal_tag` VARCHAR(8) DEFAULT NULL COMMENT '治愈标签（2-4字）' AFTER `review_reason`,
  ADD COLUMN `healing_message` TEXT DEFAULT NULL COMMENT 'AI治愈回复' AFTER `heal_tag`,
  ADD COLUMN `wish_count` INT NOT NULL DEFAULT 0 COMMENT '许愿次数' AFTER `healing_message`;

-- ============================================================
-- 许愿表
-- ============================================================
CREATE TABLE IF NOT EXISTS `wish` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '许愿ID',
  `meteor_id`  BIGINT        NOT NULL                 COMMENT '流星ID',
  `user_id`    BIGINT        DEFAULT NULL             COMMENT '许愿者用户ID',
  `content`    VARCHAR(500)  DEFAULT NULL             COMMENT '许愿内容',
  `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '许愿时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meteor` (`meteor_id`),
  INDEX `idx_user` (`user_id`),
  CONSTRAINT `fk_wish_message` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wish_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='许愿记录表';

-- ============================================================
-- 捞取历史表
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
