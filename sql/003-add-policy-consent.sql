-- ============================================================
-- 迁移脚本：添加用户协议同意字段
-- 执行方式: mysql -u root -p star_weave < sql/003-add-policy-consent.sql
-- ============================================================

ALTER TABLE `user`
  ADD COLUMN `agreed_policy` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否同意用户协议和隐私政策' AFTER `is_admin`,
  ADD COLUMN `agreed_at` DATETIME DEFAULT NULL COMMENT '同意协议时间' AFTER `agreed_policy`;

-- 已有用户默认视为同意
UPDATE `user` SET `agreed_policy` = 1, `agreed_at` = NOW() WHERE `agreed_policy` = 0;
