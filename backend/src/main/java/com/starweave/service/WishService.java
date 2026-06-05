package com.starweave.service;

import com.starweave.entity.Wish;

import java.util.List;
import java.util.Map;

public interface WishService {

    /** 发布回复（自动触发 AI 审核） */
    Wish makeWish(Long meteorId, Long userId, String content);

    /** 管理员审核回复 */
    Wish reviewWish(Long wishId, String status, String reason);

    /** 待审核回复列表 */
    List<Wish> findPendingWishes();

    /** 全部回复（可选按状态筛选） */
    List<Wish> findAllWishes(String status);

    /** 回复统计 */
    Map<String, Long> getWishStats();

    /** 管理员删除回复 */
    boolean deleteWish(Long wishId);

    /** 删除用户的所有回复 */
    boolean deleteWishesByUserId(Long userId);

    /** 用户删除自己的回复 */
    boolean deleteWishByOwner(Long wishId, Long userId);
}
