package com.starweave.service.impl;

import com.starweave.entity.Wish;
import com.starweave.mapper.MessageMapper;
import com.starweave.mapper.WishMapper;
import com.starweave.service.AiReviewService;
import com.starweave.service.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WishServiceImpl implements WishService {

    private static final Logger log = LoggerFactory.getLogger(WishServiceImpl.class);

    private final WishMapper wishMapper;
    private final MessageMapper messageMapper;
    private final AiReviewService aiReviewService;

    public WishServiceImpl(WishMapper wishMapper, MessageMapper messageMapper, AiReviewService aiReviewService) {
        this.wishMapper = wishMapper;
        this.messageMapper = messageMapper;
        this.aiReviewService = aiReviewService;
    }

    @Override
    @Transactional
    public Wish makeWish(Long meteorId, Long userId, String content) {
        Wish wish = new Wish();
        wish.setMeteorId(meteorId);
        wish.setUserId(userId);
        wish.setContent(content);
        wish.setStatus("pending");

        wishMapper.insert(wish);
        log.info("用户 {} 对流星 [{}] 发布回复 [{}] → 内容: {}", userId, meteorId, wish.getId(), content);

        // AI 审核
        AiReviewService.ReviewResult review = aiReviewService.reviewWish(wish);
        String finalStatus;
        if (review.approved()) {
            finalStatus = "approved";
        } else if (review.needsManualReview()) {
            finalStatus = "pending";
        } else {
            finalStatus = "rejected";
        }

        log.debug("回复 [{}] 审核结果: {} | 原因: {}", wish.getId(), finalStatus, review.reason());

        wishMapper.updateStatus(wish.getId(), finalStatus, review.reason());
        wish.setStatus(finalStatus);
        wish.setReviewReason(review.reason());

        // 许愿计数 +1（只有审核通过才计数）
        if ("approved".equals(finalStatus)) {
            messageMapper.incrementWishCount(meteorId);
        }

        return wishMapper.findById(wish.getId());
    }

    @Override
    @Transactional
    public Wish reviewWish(Long wishId, String status, String reason) {
        wishMapper.updateStatus(wishId, status, reason);
        return wishMapper.findById(wishId);
    }

    @Override
    public List<Wish> findPendingWishes() {
        return wishMapper.findPendingWishes();
    }

    @Override
    public List<Wish> findAllWishes(String status) {
        return wishMapper.findAllWishes(status);
    }

    @Override
    public Map<String, Long> getWishStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", wishMapper.countWishByStatus("pending"));
        stats.put("approved", wishMapper.countWishByStatus("approved"));
        stats.put("rejected", wishMapper.countWishByStatus("rejected"));
        stats.put("total", wishMapper.countWish());
        return stats;
    }

    @Override
    @Transactional
    public boolean deleteWish(Long wishId) {
        Wish wish = wishMapper.findById(wishId);
        if (wish == null) {
            throw new RuntimeException("回复不存在");
        }
        wishMapper.deleteWishById(wishId);
        log.info("管理员删除了回复 [{}]", wishId);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteWishesByUserId(Long userId) {
        wishMapper.deleteByUserId(userId);
        log.info("删除了用户 {} 的所有回复", userId);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteWishByOwner(Long wishId, Long userId) {
        Wish wish = wishMapper.findById(wishId);
        if (wish == null) {
            throw new RuntimeException("回复不存在");
        }
        if (!wish.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的回复");
        }
        wishMapper.deleteWishById(wishId);
        log.info("用户 {} 删除了自己的回复 [{}]", userId, wishId);
        return true;
    }
}
