package com.starweave.service;

import com.starweave.entity.Message;
import com.starweave.entity.Wish;

/**
 * AI 内容审核服务
 * 接口设计为可替换：初期用关键词过滤，未来可接入 LLM API
 */
public interface AiReviewService {

    /**
     * 审核消息内容
     * @param message 待审核消息
     * @return ReviewResult 审核结果
     */
    ReviewResult review(Message message);

    /**
     * 审核回复内容
     * @param wish 待审核回复
     * @return ReviewResult 审核结果
     */
    ReviewResult reviewWish(Wish wish);

    record ReviewResult(
            boolean approved,
            double confidence,
            String reason,
            String healTag,
            String healingMessage
    ) {
        /**
         * @return 是否需要管理员人工审核
         */
        public boolean needsManualReview() {
            return !approved && confidence < 0.8;
        }
    }
}
