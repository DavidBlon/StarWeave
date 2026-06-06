USE star_weave;

CREATE TABLE IF NOT EXISTS `afdian_account_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `user_id` BIGINT NOT NULL COMMENT '本站用户ID',
  `afdian_user_id` VARCHAR(100) NOT NULL COMMENT '爱发电用户ID',
  `afdian_user_private_id` VARCHAR(100) DEFAULT NULL COMMENT '爱发电用户私有ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_afdian_user_id` (`afdian_user_id`),
  KEY `idx_afdian_user_private_id` (`afdian_user_private_id`),
  CONSTRAINT `fk_afdian_link_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爱发电账号绑定表';
