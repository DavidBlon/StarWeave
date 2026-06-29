package com.starweave.config;

import com.starweave.entity.User;
import com.starweave.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时自动创建超级管理员账号（如果不存在）
 *
 * 登录凭据: admin / admin888
 * 登录后在底部导航栏会多出一个「审核」tab
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserMapper userMapper;

    public AdminInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        // 检查是否已有管理员账号（通过 username 或 nickname）
        User existing = userMapper.findByUsername("admin");
        if (existing == null) {
            existing = userMapper.findByNickname("admin");
        }
        if (existing != null) {
            // 确保 isAdmin 标记为 true
            boolean changed = false;
            if (!Boolean.TRUE.equals(existing.getIsAdmin())) {
                existing.setIsAdmin(true);
                changed = true;
            }
            // 迁移旧版 SHA-256 哈希 → BCrypt（旧哈希是 64 位 hex，BCrypt 以 $2 开头）
            String ph = existing.getPasswordHash();
            if (ph != null && !ph.startsWith("$2")) {
                existing.setPasswordHash(passwordEncoder.encode("admin888"));
                log.info("迁移管理员密码哈希: SHA-256 → BCrypt");
                changed = true;
            }
            if (changed) {
                userMapper.update(existing);
            }
            log.debug("管理员账号已就绪 (id={})", existing.getId());
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setNickname("管理员");
        admin.setPasswordHash(passwordEncoder.encode("admin888"));
        admin.setBio("✦ 星海管理者");
        admin.setBorderStyle("admin");
        admin.setIsSponsor(false);
        admin.setIsAdmin(true);
        admin.setAgreedPolicy(true);
        admin.setAgreedAt(LocalDateTime.now());
        admin.setTokenVersion(0);
        admin.setAvatarUrl(null);

        userMapper.insert(admin);
        log.info("✦ 超级管理员账号已创建 (id={})", admin.getId());
        log.info("✦ 登录凭据: admin / admin888");
        log.info("✦ 登录后底部导航将出现「审核」tab");
    }
}
