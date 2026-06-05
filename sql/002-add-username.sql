-- ============================================================
-- 流星树洞 · 织星海 — 分离用户名与昵称
-- ============================================================
-- username: 登录凭证，不可修改
-- nickname: 显示名字，可自由修改
-- ============================================================

USE star_weave;

-- 1. 新增 username 列（放在 id 后面）
ALTER TABLE `user`
  ADD COLUMN `username` VARCHAR(50) NOT NULL COMMENT '登录用户名（唯一，不可修改）' AFTER `id`;

-- 2. 迁移现有数据：把当前 nickname 复制到 username
UPDATE `user` SET `username` = `nickname`;

-- 3. 创建唯一索引
CREATE UNIQUE INDEX `idx_username` ON `user`(`username`);
