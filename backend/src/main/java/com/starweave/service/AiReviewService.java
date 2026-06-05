package com.starweave.service;

import com.starweave.entity.Message;

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
