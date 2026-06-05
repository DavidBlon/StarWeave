-- ============================================================
-- 流星树洞 · 织星海 — 完整建库脚本（整合版）
-- ============================================================
-- 使用方式:
--   mysql -u root -p < sql/init.sql
-- 或登录后:
--   source sql/init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS star_weave
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE star_weave;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
  `username`      VARCHAR(50)   NOT NULL                 COMMENT '登录用户名（唯一，不可修改）',
  `nickname`      VARCHAR(50)   NOT NULL                 COMMENT '显示昵称',
  `avatar_url`    TEXT          DEFAULT NULL             COMMENT '头像地址（base64 或 emoji 标识）',
  `bio`           VARCHAR(200)  DEFAULT NULL             COMMENT '个人签名',
  `password_hash` VARCHAR(255)  DEFAULT NULL             COMMENT '密码哈希（SHA-256，可空）',
  `border_style`  VARCHAR(50)   DEFAULT 'default'        COMMENT '星图边框样式（赞助权益）',
  `is_sponsor`    TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否为赞助者',
  `is_admin`      TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '是否为管理员',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`),
  INDEX `idx_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 流星消息表（树洞核心内容）
-- ============================================================
CREATE TABLE IF NOT EXISTS `message` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  `user_id`         BIGINT        NOT NULL                 COMMENT '发布者用户ID',
  `content`         TEXT          NOT NULL                 COMMENT '消息内容',
  `color`           VARCHAR(20)   DEFAULT '#FFD700'        COMMENT '流星颜色',
  `status`          ENUM('pending','approved','rejected')
                    NOT NULL DEFAULT 'pending'             COMMENT 'AI审核状态',
  `review_reason`   VARCHAR(255)  DEFAULT NULL             COMMENT '审核拒绝原因',
  `heal_tag`        VARCHAR(8)    DEFAULT NULL             COMMENT '治愈标签（2-4字）',
  `healing_message` TEXT          DEFAULT NULL             COMMENT 'AI治愈回复',
  `wish_count`      INT           NOT NULL DEFAULT 0       COMMENT '许愿次数',
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
-- 星图表
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
-- 许愿 / 回复表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='许愿 / 回复表';

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

-- ============================================================
-- 星光守护者表（赞助记录）
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
-- AI 审核日志表
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
-- 初始数据：星光守护者样例
-- ============================================================
INSERT INTO `sponsor` (`display_name`, `message`, `border_style`, `amount`, `platform`, `is_active`) VALUES
  ('星野',   '愿每一颗流星都找到归处',                                 'sponsor', 66.00, 'afdian', 1),
  ('夜航船', '在黑暗中为你点亮一束光',                                 'sponsor', 33.00, 'afdian', 1),
  ('阿九',   '星星发亮是为了让每一个人有一天都能找到属于自己的星星', 'sponsor', 99.00, 'afdian', 1),
  ('鹿鸣',   '野鹿鸣啾啾，星河入梦来',                               'sponsor', 18.00, 'afdian', 1),
  ('白鲸',   '深海有鲸，夜空有星',                                   'sponsor', 52.00, 'afdian', 1);

-- ============================================================
-- 管理员账号（密码: admin888）
-- Java AdminInitializer 启动时也会自动创建，这里手动兜底
-- ============================================================
INSERT INTO `user` (`username`, `nickname`, `password_hash`, `bio`, `border_style`, `is_sponsor`, `is_admin`)
VALUES ('admin', '管理员', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '✦ 星海管理者', 'admin', 0, 1);

-- ============================================================
-- 建库完成
-- ============================================================
