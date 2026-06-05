-- ============================================================
-- 回复（wish）审核字段迁移
-- ============================================================

ALTER TABLE `wish`
  ADD COLUMN `status`        ENUM('pending','approved','rejected')
                             NOT NULL DEFAULT 'approved'
                             COMMENT 'AI审核状态' AFTER `content`,
  ADD COLUMN `review_reason` VARCHAR(255) DEFAULT NULL
                             COMMENT '审核拒绝原因' AFTER `status`,
  ADD COLUMN `reviewed_at`   DATETIME     DEFAULT NULL
                             COMMENT '审核时间' AFTER `review_reason`;

-- 现有回复默认为已通过
UPDATE `wish` SET `status` = 'approved' WHERE `status` IS NULL;
