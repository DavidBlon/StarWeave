package com.starweave.service.impl;

import com.starweave.entity.AiReviewLog;
import com.starweave.entity.Message;
import com.starweave.mapper.AiReviewLogMapper;
import com.starweave.service.AiReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 基于 DeepSeek API 的真实 AI 审核实现
 *
 * 工作流程:
 * 1. 调用 DeepSeek 对内容进行语义审核
 * 2. 根据 AI 返回的判断决定:
 *    - approved: 明显合规 → 自动通过
 *    - rejected: 明显违规 → 自动拒绝
 *    - pending:  不确定/边缘情况 → 转人工审核
 * 3. 如果 API 调用失败，安全降级为 pending（人工兜底）
 */
@Primary
@Service
public class DeepSeekReviewServiceImpl implements AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekReviewServiceImpl.class);

    private final RestTemplate restTemplate;
    private final AiReviewLogMapper reviewLogMapper;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    /** 审核通过的置信度阈值 — 低于此值转人工 */
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    /** 明确拒绝的置信度阈值 — 高于此值自动拒绝 */
    private static final double REJECT_THRESHOLD = 0.85;

    /** 治愈标签库 */
    private static final List<HealingOption> HEALING_OPTIONS = List.of(
            new HealingOption("会好的",   "一切都会好起来的，给自己一点时间，让星光治愈你的心。"),
            new HealingOption("加油",    "你比你想象中更坚强，每一个努力的你都在闪闪发光。"),
            new HealingOption("抱抱你",  "虽然隔着星河，但请收下这个温暖的拥抱。"),
            new HealingOption("放下了",  "有些事，放下了才能重新出发。你已经做得很好了。"),
            new HealingOption("想开点",  "换个角度看看，也许没有想象中那么糟糕。"),
            new HealingOption("慢慢来",  "放慢脚步也没关系，星河一直在那里等你。"),
            new HealingOption("我懂",    "你的感受是真实的，有人和你一样在经历着。"),
            new HealingOption("没关系",  "犯错也没关系，成长本来就是磕磕绊绊的过程。")
    );

    public DeepSeekReviewServiceImpl(AiReviewLogMapper reviewLogMapper, RestTemplateBuilder builder) {
        this.reviewLogMapper = reviewLogMapper;
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    @Transactional
    public ReviewResult review(Message message) {
        String content = message.getContent();

        // 1. 基础长度检查
        if (content == null || content.isBlank()) {
            return reject("内容为空", 1.0);
        }
        if (content.length() > 2000) {
            return reject("内容超出长度限制", 0.95);
        }

        // 2. 调用 DeepSeek API 进行语义审核
        try {
            return callDeepSeekReview(message);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败，降级为人工审核 (messageId={}): {}", message.getId(), e.getMessage());
            // API 失败时安全降级为 pending，由管理员审核
            return pending("AI 审核服务暂时不可用，已转人工", 0.5);
        }
    }

    /**
     * 调用 DeepSeek API 进行语义审核
     */
    private ReviewResult callDeepSeekReview(Message message) {
        // 构建审核 prompt
        String systemPrompt = """
                你是流星树洞的内容审核员。你的任务是判断用户匿名倾诉的内容是否适合公开在树洞中。

                【审核原则】
                - ✅ 允许：情感倾诉、烦恼、心事、碎碎念、生活分享、含有轻微负面情绪的内容
                - ❌ 拒绝：色情内容、约炮/援交、赌博、毒品、暴力、仇恨言论、广告推广、诈骗信息、政治敏感内容、联系方式（微信/QQ/手机号/链接）
                - ⚠️ 不确定：如果内容介于两者之间，或者你无法明确判断，请标记为 pending

                【输出格式】
                请严格按以下 JSON 格式返回，不要包含其他文字：
                {
                  "decision": "approve" 或 "reject" 或 "pending",
                  "confidence": 0.0~1.0的小数,
                  "reason": "判断理由，一句话说明"
                }

                注意：
                - confidence >= 0.9 表示非常确定
                - confidence 在 0.7~0.89 表示有一定把握
                - confidence < 0.7 表示不太确定，应设为 pending
                - 含有色情、暴力、广告等明显违规内容直接 reject
                - 纯粹的情感倾诉、烦恼倾诉，即使有点丧，也应 approve
                """;

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "请审核以下内容：\n" + message.getContent())
                ),
                "temperature", 0.1,
                "max_tokens", 300
        );

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, Map.class
        );

        // 解析响应
        String decision = "pending";
        double confidence = 0.0;
        String reason = "AI 审核无法确定，请管理员人工判断";

        try {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> chatMessage = (Map<String, Object>) choice.get("message");
                    String aiContent = (String) chatMessage.get("content");

                    // 提取 JSON
                    ReviewAiResult aiResult = parseAiResponse(aiContent);
                    decision = aiResult.decision;
                    confidence = aiResult.confidence;
                    reason = aiResult.reason;
                }
            }
        } catch (Exception e) {
            log.warn("解析 DeepSeek 响应失败: {}", e.getMessage());
        }

        // 根据 decision 和 confidence 做出最终判断
        return makeFinalDecision(message.getId(), decision, confidence, reason);
    }

    /**
     * 解析 AI 返回的 JSON
     */
    private ReviewAiResult parseAiResponse(String content) {
        String decision = "pending";
        double confidence = 0.0;
        String reason = "AI 审核无法确定，请管理员人工判断";

        try {
            // 尝试从 ```json ... ``` 代码块中提取
            if (content.contains("```")) {
                content = content.replaceAll("(?s)```(json)?", "").trim();
            }

            // 找第一个 { 和最后一个 }
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                content = content.substring(start, end + 1);
            }

            // 简单 JSON 解析（不依赖 Jackson 的树模型复杂解析）
            content = content.trim();
            if (content.startsWith("{") && content.endsWith("}")) {
                // 去掉花括号，按逗号分割键值对
                String inner = content.substring(1, content.length() - 1).trim();

                // 提取 decision
                if (inner.contains("\"decision\"")) {
                    int idx = inner.indexOf("\"decision\"");
                    String after = inner.substring(idx + 10);
                    if (after.contains("\"")) {
                        int q1 = after.indexOf('"') + 1;
                        int q2 = after.indexOf('"', q1);
                        if (q1 > 0 && q2 > q1) {
                            decision = after.substring(q1, q2);
                        }
                    }
                }

                // 提取 confidence
                if (inner.contains("\"confidence\"")) {
                    int idx = inner.indexOf("\"confidence\"");
                    String after = inner.substring(idx + 12);
                    // 找数字 (可以是 0.85 或 1 等形式)
                    StringBuilder num = new StringBuilder();
                    boolean found = false;
                    for (char c : after.toCharArray()) {
                        if (Character.isDigit(c) || c == '.') {
                            num.append(c);
                            found = true;
                        } else if (found) {
                            break;
                        }
                    }
                    if (!num.isEmpty()) {
                        confidence = Double.parseDouble(num.toString());
                        if (confidence > 1.0) confidence = 1.0;
                    }
                }

                // 提取 reason
                if (inner.contains("\"reason\"")) {
                    int idx = inner.indexOf("\"reason\"");
                    String after = inner.substring(idx + 8);
                    int q1 = after.indexOf('"');
                    if (q1 >= 0) {
                        int q2 = after.indexOf('"', q1 + 1);
                        if (q2 > q1) {
                            reason = after.substring(q1 + 1, q2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("AI 响应解析失败，原始内容: {}", content);
        }

        return new ReviewAiResult(decision, confidence, reason);
    }

    /**
     * 根据 AI 的判断做出最终审核决定
     */
    private ReviewResult makeFinalDecision(Long messageId, String decision, double confidence, String reason) {
        ReviewResult result;

        switch (decision) {
            case "reject":
                if (confidence >= REJECT_THRESHOLD) {
                    // 高置信度违规 → 自动拒绝
                    result = new ReviewResult(false, confidence, reason, null, null);
                } else if (confidence >= CONFIDENCE_THRESHOLD) {
                    // 中等置信度 → 转人工
                    result = new ReviewResult(false, confidence,
                            "AI 判定可能违规（可信度 " + String.format("%.0f", confidence * 100) + "%），转人工复核: " + reason,
                            null, null);
                    // 标注为 pending 让管理员复核
                    result = new ReviewResult(false, confidence,
                            "AI 认为可能违规，转人工复核: " + reason,
                            null, null);
                    // 覆盖 status 为 pending 的特殊处理 — 在 publish 方法里判断
                } else {
                    // 低置信度 → 转人工
                    result = new ReviewResult(false, confidence,
                            "AI 不太确定，转人工审核: " + reason,
                            null, null);
                }
                break;

            case "approve":
                if (confidence >= CONFIDENCE_THRESHOLD) {
                    // 高置信度合规 → 自动通过
                    HealingOption healing = HEALING_OPTIONS.get((int)(Math.random() * HEALING_OPTIONS.size()));
                    result = new ReviewResult(true, confidence, reason, healing.tag, healing.message);
                } else {
                    // 低置信度合规 → 转人工以防万一
                    result = new ReviewResult(false, confidence,
                            "AI 认为内容可能合规但不太确定，转人工审核: " + reason,
                            null, null);
                }
                break;

            case "pending":
            default:
                // AI 主动表示不确定
                result = new ReviewResult(false, confidence,
                        "AI 无法确定，转人工审核: " + reason,
                        null, null);
                break;
        }

        // 记录审核日志
        saveLog(messageId, result);

        log.debug("DeepSeek 审核 messageId={} → decision={}, confidence={}, reason={}",
                messageId, result.approved() ? "approved" : "rejected",
                String.format("%.2f", result.confidence()), result.reason());

        return result;
    }

    private ReviewResult reject(String reason, double confidence) {
        ReviewResult result = new ReviewResult(false, confidence, reason, null, null);
        log.debug("审核拒绝: {} (confidence={})", reason, confidence);
        return result;
    }

    private ReviewResult pending(String reason, double confidence) {
        ReviewResult result = new ReviewResult(false, confidence, reason, null, null);
        log.debug("审核挂起转人工: {} (confidence={})", reason, confidence);
        return result;
    }

    private void saveLog(Long messageId, ReviewResult result) {
        try {
            AiReviewLog log = new AiReviewLog();
            log.setMessageId(messageId);
            log.setResult(result.approved() ? "approved" : "rejected");
            log.setConfidence(BigDecimal.valueOf(result.confidence()));
            log.setReason(result.reason());
            reviewLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("保存审核日志失败: {}", e.getMessage());
        }
    }

    /**
     * AI 响应解析结果
     */
    private record ReviewAiResult(String decision, double confidence, String reason) {}

    /**
     * 治愈标签
     */
    private record HealingOption(String tag, String message) {}
}
