package com.starweave.config;

import com.starweave.entity.User;
import com.starweave.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 应用启动时自动创建超级管理员账号（如果不存在）
 *
 * 登录凭据: admin / admin888
 * 登录后在底部导航栏会多出一个「审核」tab
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

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
            if (!Boolean.TRUE.equals(existing.getIsAdmin())) {
                existing.setIsAdmin(true);
                userMapper.update(existing);
                log.debug("✦ 已升级用户 'admin' (id={}) 为管理员", existing.getId());
            } else {
                log.debug("管理员账号已存在 (id={})", existing.getId());
            }
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setNickname("admin");
        admin.setPasswordHash(hashPassword("admin888"));
        admin.setBio("✦ 星海管理者");
        admin.setBorderStyle("admin");
        admin.setIsSponsor(false);
        admin.setIsAdmin(true);
        admin.setAgreedPolicy(true);
        admin.setAgreedAt(LocalDateTime.now());
        admin.setAvatarUrl(null);

        userMapper.insert(admin);
        log.debug("✦ 超级管理员账号已创建 (id={})", admin.getId());
        log.debug("✦ 登录凭据: admin / admin888");
        log.debug("✦ 登录后底部导航将出现「审核」tab");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}
