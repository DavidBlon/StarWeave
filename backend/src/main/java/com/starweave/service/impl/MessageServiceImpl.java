package com.starweave.service.impl;

import com.starweave.entity.Message;
import com.starweave.mapper.MessageMapper;
import com.starweave.mapper.StarMapMapper;
import com.starweave.mapper.WishMapper;
import com.starweave.entity.Wish;
import com.starweave.entity.CatchHistory;
import com.starweave.mapper.CatchHistoryMapper;
import com.starweave.service.AiReviewService;
import com.starweave.service.MessageService;
import com.starweave.service.StarMapService;
import com.starweave.service.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageMapper messageMapper;
    private final WishMapper wishMapper;
    private final AiReviewService aiReviewService;
    private final StarMapService starMapService;
    private final StarMapMapper starMapMapper;
    private final CatchHistoryMapper catchHistoryMapper;
    private final WishService wishService;
    private final Random random = new Random();

    public MessageServiceImpl(MessageMapper messageMapper,
                              WishMapper wishMapper,
                              AiReviewService aiReviewService,
                              StarMapService starMapService,
                              StarMapMapper starMapMapper,
                              CatchHistoryMapper catchHistoryMapper,
                              WishService wishService) {
        this.messageMapper = messageMapper;
        this.wishMapper = wishMapper;
        this.aiReviewService = aiReviewService;
        this.starMapService = starMapService;
        this.starMapMapper = starMapMapper;
        this.catchHistoryMapper = catchHistoryMapper;
        this.wishService = wishService;
    }

    @Override
    public Message findById(Long id) {
        return messageMapper.findById(id);
    }

    @Override
    public List<Message> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return messageMapper.findByIds(ids);
    }

    @Override
    public List<Message> findFloating(int limit) {
        return messageMapper.findFloating(limit);
    }

    @Override
    public Message findRandomApproved(Long excludeUserId) {
        MessageMapper.IdRange range = messageMapper.findApprovedIdRange(excludeUserId);
        if (range == null || range.minId() == 0) return null;
        // 在 [minId, maxId] 范围内随机选一个起点，取第一条满足条件的流星
        long targetId = range.minId() + (long) (random.nextDouble() * (range.maxId() - range.minId() + 1));
        return messageMapper.findRandomApproved(targetId, excludeUserId);
    }

    @Override
    public List<Message> findByUserId(Long userId) {
        return messageMapper.findByUserId(userId);
    }

    @Override
    public List<Message> findByCatcher(Long userId) {
        return messageMapper.findByCatcher(userId);
    }

    @Override
    public List<Message> findPending() {
        return messageMapper.findPending();
    }

    @Override
    @Transactional
    public Message publish(Long userId, String content, String color) {
        Message message = new Message();
        message.setUserId(userId);
        message.setContent(content);
        message.setColor(color != null ? color : "#8be9fd");
        message.setStatus("pending");
        message.setIsCaught(false);
        message.setWishCount(0);

        messageMapper.insert(message);

        log.info("用户 {} 发布流星 [{}] → 内容: {} | 颜色: {}", userId, message.getId(), content, color);

        // AI 审核
        AiReviewService.ReviewResult review = aiReviewService.review(message);
        String finalStatus;
        if (review.approved()) {
            finalStatus = "approved";
        } else if (review.needsManualReview()) {
            finalStatus = "pending";
        } else {
            finalStatus = "rejected";
        }

        log.debug("流星 [{}] 审核结果: {} | 原因: {} | 治愈标签: {}", message.getId(), finalStatus, review.reason(), review.healTag());

        messageMapper.updateStatus(
                message.getId(),
                finalStatus,
                review.reason(),
                review.healTag(),
                review.healingMessage()
        );
        message.setStatus(finalStatus);
        message.setReviewReason(review.reason());
        message.setHealTag(review.healTag());
        message.setHealingMessage(review.healingMessage());

        // 审核通过则自动生成星图
        if (review.approved()) {
            starMapService.generate(message.getId(), userId, content);
            log.debug("流星 [{}] 审核通过 → 星图已生成", message.getId());
        }

        return messageMapper.findById(message.getId());
    }

    @Override
    @Transactional
    public boolean catchMeteor(Long messageId, Long userId) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            log.warn("用户 {} 尝试捞取不存在的流星 [{}]", userId, messageId);
            throw new RuntimeException("流星不存在");
        }
        if (message.getUserId().equals(userId)) {
            log.warn("用户 {} 试图捞取自己的流星 [{}]", userId, messageId);
            throw new RuntimeException("不能捞起自己的流星");
        }
        boolean ok = messageMapper.catchMessage(messageId, userId) > 0;
        if (ok) {
            CatchHistory history = new CatchHistory();
            history.setUserId(userId);
            history.setMeteorId(messageId);
            catchHistoryMapper.insert(history);
            log.info("用户 {} 捞起了流星 [{}]（发布者: {}）", userId, messageId, message.getUserId());
        } else {
            log.warn("用户 {} 捞取流星 [{}] 失败 — 已被他人捞走", userId, messageId);
        }
        return ok;
    }

    @Override
    @Transactional
    public Message review(Long messageId, String status, String reason) {
        messageMapper.updateStatus(messageId, status, reason, null, null);
        return messageMapper.findById(messageId);
    }

    @Override
    public List<Message> findAllMessages(String status) {
        return messageMapper.findAll(status);
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", messageMapper.countByStatus("pending"));
        stats.put("approved", messageMapper.countByStatus("approved"));
        stats.put("rejected", messageMapper.countByStatus("rejected"));
        stats.put("total", messageMapper.count());
        return stats;
    }

    @Override
    @Transactional
    public boolean deleteMeteor(Long messageId, Long userId) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            throw new RuntimeException("流星不存在");
        }
        // userId 为 null 表示管理员操作，跳过权限检查
        if (userId != null && !message.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的流星");
        }
        // 删除关联数据
        wishMapper.deleteByMeteorId(messageId);
        starMapMapper.deleteByMessageId(messageId);
        catchHistoryMapper.deleteByMeteorId(messageId);
        messageMapper.deleteById(messageId);
        log.info("删除流星 [{}] (操作者: {})", messageId, userId != null ? userId : "管理员");
        return true;
    }

    @Override
    @Transactional
    public boolean makeWish(Long meteorId, Long userId, String content) {
        // 委托给 WishService 处理（含 AI 审核）
        wishService.makeWish(meteorId, userId, content);
        return true;
    }
}
