package com.starweave.service;

import com.starweave.entity.User;

import java.util.List;

public interface UserService {

    User findById(Long id);

    User findByNickname(String nickname);

    User findByUsername(String username);

    List<User> findAll();

    /** 使用密码注册 */
    User registerWithPassword(String username, String nickname, String password);

    /** 使用密码登录 */
    User loginWithPassword(String username, String password);

    boolean setSponsorStatus(Long userId, boolean isSponsor);

    /** 更新昵称和个人签名 */
    User updateProfile(Long userId, String nickname, String bio);

    /** 更新头像 */
    User updateAvatar(Long userId, String avatarUrl);

    /** 修改密码 */
    User changePassword(Long userId, String oldPassword, String newPassword);

    /** 获取用户统计数据 */
    com.starweave.dto.UserStats getUserStats(Long userId);

    /** 删除用户及其所有关联数据（管理员用） */
    boolean deleteUser(Long userId);
}
