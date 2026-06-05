-- ============================================================
-- 流星树洞 · 织星海 — 添加管理员支持
-- ============================================================
-- 使用方式：source sql/001-add-admin.sql
-- 管理员会自动由 Java AdminInitializer 在启动时创建
-- 如果你需要手动创建，可以用下面的 SQL：

-- 1. user 表添加 is_admin 字段
ALTER TABLE `user`
  ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为管理员'
  AFTER `is_sponsor`;

-- 2. 如需手动创建管理员（启动时也会自动创建）：
-- 密码: admin888 (SHA-256: 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918)
-- INSERT INTO `user` (`nickname`, `password_hash`, `bio`, `border_style`, `is_sponsor`, `is_admin`)
-- VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '✦ 星海管理者', 'admin', 0, 1);
