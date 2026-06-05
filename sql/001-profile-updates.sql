-- ============================================================
-- 流星树洞 · 织星海 — 个人页功能补全
-- ============================================================

USE star_weave;

-- 1. 用户表新增 bio 字段
ALTER TABLE `user`
  ADD COLUMN `bio`        VARCHAR(200) DEFAULT NULL COMMENT '个人签名' AFTER `avatar_url`;

-- 2. 创建许愿表（仅当不存在）
CREATE TABLE IF NOT EXISTS `wish` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '许愿ID',
  `meteor_id`     BIGINT        NOT NULL                 COMMENT '流星ID',
  `user_id`       BIGINT        NOT NULL                 COMMENT '许愿者ID',
  `content`       VARCHAR(500)  NOT NULL                 COMMENT '许愿内容',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '许愿时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meteor` (`meteor_id`),
  INDEX `idx_user` (`user_id`),
  CONSTRAINT `fk_wish_meteor` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wish_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='许愿/回复表';

-- 3. 创建捞取历史表（仅当不存在）
CREATE TABLE IF NOT EXISTS `catch_history` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
  `user_id`       BIGINT        NOT NULL                 COMMENT '捞取者ID',
  `meteor_id`     BIGINT        NOT NULL                 COMMENT '捞取的流星ID',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捞取时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_meteor` (`meteor_id`),
  CONSTRAINT `fk_catch_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_catch_meteor` FOREIGN KEY (`meteor_id`) REFERENCES `message`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捞取历史表';
