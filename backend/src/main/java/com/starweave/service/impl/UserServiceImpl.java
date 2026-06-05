package com.starweave.service.impl;

import com.starweave.dto.UserStats;
import com.starweave.entity.User;
import com.starweave.mapper.CatchHistoryMapper;
import com.starweave.mapper.MessageMapper;
import com.starweave.mapper.StarMapMapper;
import com.starweave.mapper.UserMapper;
import com.starweave.mapper.WishMapper;
import com.starweave.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final WishMapper wishMapper;
    private final StarMapMapper starMapMapper;
    private final CatchHistoryMapper catchHistoryMapper;

    public UserServiceImpl(UserMapper userMapper, MessageMapper messageMapper, WishMapper wishMapper,
                           StarMapMapper starMapMapper, CatchHistoryMapper catchHistoryMapper) {
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
        this.wishMapper = wishMapper;
        this.starMapMapper = starMapMapper;
        this.catchHistoryMapper = catchHistoryMapper;
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User findByNickname(String nickname) {
        return userMapper.findByNickname(nickname);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    @Transactional
    public User register(String nickname) {
        User user = new User();
        user.setUsername(nickname);
        user.setNickname(nickname);
        user.setBorderStyle("default");
        user.setIsSponsor(false);
        user.setIsAdmin(false);
        user.setAgreedPolicy(true);
        user.setAgreedAt(LocalDateTime.now());
        user.setPasswordHash(null);
        int avatarSeed = nickname.hashCode() & 0x7fffffff;
        user.setAvatarUrl("/api/avatar/" + (avatarSeed % 100));

        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User registerWithPassword(String username, String nickname, String password) {
        // 检查用户名是否已存在
        User existing = userMapper.findByUsername(username);
        if (existing != null) {
            throw new RuntimeException("该用户名已被使用");
        }

        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname != null && !nickname.isBlank() ? nickname.strip() : username);
        user.setBorderStyle("default");
        user.setIsSponsor(false);
        user.setIsAdmin(false);
        user.setAgreedPolicy(true);
        user.setAgreedAt(LocalDateTime.now());
        user.setPasswordHash(hashPassword(password));
        int avatarSeed = username.hashCode() & 0x7fffffff;
        user.setAvatarUrl("/api/avatar/" + (avatarSeed % 100));

        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User loginWithPassword(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPasswordHash() == null) {
            throw new RuntimeException("请设置密码后再登录");
        }
        if (!user.getPasswordHash().equals(hashPassword(password))) {
            throw new RuntimeException("密码错误");
        }
        // 登录时更新协议同意状态
        user.setAgreedPolicy(true);
        user.setAgreedAt(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    @Override
    @Transactional
    public boolean setSponsorStatus(Long userId, boolean isSponsor) {
        return userMapper.setSponsor(userId, isSponsor) > 0;
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String nickname, String bio) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickname != null && !nickname.isBlank()) {
            if (nickname.length() > 20) {
                throw new RuntimeException("昵称最长 20 个字符");
            }
            // 检查昵称是否被占用（排除自己）
            User existing = userMapper.findByNickname(nickname);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new RuntimeException("该昵称已被使用");
            }
            user.setNickname(nickname.strip());
        }
        if (bio != null) {
            if (bio.length() > 200) {
                throw new RuntimeException("个人签名最长 200 个字符");
            }
            user.setBio(bio.strip());
        }
        userMapper.update(user);
        return userMapper.findById(userId);
    }

    @Override
    @Transactional
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setAvatarUrl(avatarUrl);
        userMapper.update(user);
        return userMapper.findById(userId);
    }

    @Override
    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPasswordHash() == null) {
            throw new RuntimeException("请先设置密码后才能修改");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码至少 6 位");
        }
        if (!user.getPasswordHash().equals(hashPassword(oldPassword))) {
            throw new RuntimeException("旧密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }
        user.setPasswordHash(hashPassword(newPassword));
        userMapper.update(user);
        return userMapper.findById(userId);
    }

    @Override
    public UserStats getUserStats(Long userId) {
        long publishedCount = messageMapper.countByUserId(userId);
        long caughtCount = messageMapper.countCaughtByUserId(userId);
        long wishCount = wishMapper.countByUserId(userId);
        return new UserStats(publishedCount, caughtCount, wishCount);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new RuntimeException("不能删除管理员账号");
        }
        // 级联删除用户所有数据
        wishMapper.deleteByUserId(userId);
        catchHistoryMapper.deleteByUserId(userId);
        starMapMapper.deleteByUserId(userId);
        messageMapper.deleteByUserId(userId);
        userMapper.deleteById(userId);
        log.info("管理员删除了用户 [{}] ({})", userId, user.getNickname());
        return true;
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
