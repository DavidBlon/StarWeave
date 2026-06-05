package com.starweave.service;

import com.starweave.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Message findById(Long id);

    /** 批量查询流星 */
    List<Message> findByIds(List<Long> ids);

    /** 获取漂流中的流星（已审核通过、未被捞起） */
    List<Message> findFloating(int limit);

    /** 随机捞取一颗已审核的流星（排除 excludeUserId 自己的流星） */
    Message findRandomApproved(Long excludeUserId);

    /** 获取用户发布的流星 */
    List<Message> findByUserId(Long userId);

    /** 获取用户捞起的流星 */
    List<Message> findByCatcher(Long userId);

    /** 待审核列表（管理员用） */
    List<Message> findPending();

    /** 获取全部流星，status 传 null 则全部（管理员用） */
    List<Message> findAllMessages(String status);

    /** 各状态统计 */
    Map<String, Long> getStats();

    /** 发布流星（会自动触发 AI 审核） */
    Message publish(Long userId, String content, String color);

    /** 捞起流星 */
    boolean catchMeteor(Long messageId, Long userId);

    /** 审核流星 */
    Message review(Long messageId, String status, String reason);

    /** 对流星许愿 */
    boolean makeWish(Long meteorId, Long userId, String content);

    /** 删除流星（仅发布者可删除） */
    boolean deleteMeteor(Long messageId, Long userId);
}
