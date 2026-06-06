package com.starweave.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starweave.entity.AiReviewLog;
import com.starweave.entity.Message;
import com.starweave.entity.Wish;
import com.starweave.mapper.AiReviewLogMapper;
import com.starweave.service.AiReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DeepSeek-backed content review.
 *
 * For published meteors, the same AI call also asks for a short healing response.
 * If AI output cannot be parsed, review falls back to manual review; if only the
 * healing fields are missing, approved content falls back to local preset copy.
 */
@Primary
@Service
public class DeepSeekReviewServiceImpl implements AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekReviewServiceImpl.class);

    private static final double CONFIDENCE_THRESHOLD = 0.8;
    private static final double REJECT_THRESHOLD = 0.85;

    private static final List<HealingOption> HEALING_OPTIONS = List.of(
            new HealingOption("会好的", "一切都会好起来的，给自己一点时间，让星光慢慢照亮心里的角落。"),
            new HealingOption("加油", "你比自己想象中更坚韧，今晚的疲惫也值得被温柔接住。"),
            new HealingOption("抱抱你", "隔着星河也想给你一个拥抱，先允许自己慢慢缓一缓。"),
            new HealingOption("放下点", "有些事可以先放一放，你已经走到这里，不必立刻给自己答案。"),
            new HealingOption("慢慢来", "不用急着变好，星河一直在，愿你今晚能轻一点。"),
            new HealingOption("我懂", "你的感受是真实的，有人看见了，也愿意认真接住这一刻。"),
            new HealingOption("没关系", "偶尔碎掉也没关系，成长本来就会有不那么体面的时刻。")
    );

    private final RestTemplate restTemplate;
    private final AiReviewLogMapper reviewLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

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
        ReviewResult basic = validateContent(content);
        if (basic != null) {
            return basic;
        }

        try {
            return callDeepSeekReview(message.getId(), content, true,
                    "你是流星树洞的内容审核员。判断用户匿名倾诉内容是否适合公开展示。");
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败，降级为人工审核 (messageId={}): {}", message.getId(), e.getMessage());
            return pending("AI 审核服务暂时不可用，已转人工", 0.5);
        }
    }

    @Override
    @Transactional
    public ReviewResult reviewWish(Wish wish) {
        String content = wish.getContent();
        ReviewResult basic = validateContent(content);
        if (basic != null) {
            return basic;
        }

        try {
            return callDeepSeekReview(wish.getMeteorId(), content, false,
                    "你是流星树洞的内容审核员。判断用户对流星的回复/许愿内容是否适合展示。");
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败，降级为人工审核 (wishId={}): {}", wish.getId(), e.getMessage());
            return pending("AI 审核服务暂时不可用，已转人工", 0.5);
        }
    }

    private ReviewResult validateContent(String content) {
        if (content == null || content.isBlank()) {
            return reject("内容为空", 1.0);
        }
        if (content.length() > 2000) {
            return reject("内容超出长度限制", 0.95);
        }
        return null;
    }

    private ReviewResult callDeepSeekReview(Long entityId, String content, boolean includeHealing, String roleDesc) {
        String systemPrompt = buildSystemPrompt(roleDesc, includeHealing);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "请审核以下内容：\n" + content)
                ),
                "temperature", includeHealing ? 0.35 : 0.1,
                "max_tokens", includeHealing ? 500 : 300
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        ReviewAiResult aiResult = parseApiResponse(response.getBody(), includeHealing);
        return makeFinalDecision(entityId, aiResult, includeHealing);
    }

    private String buildSystemPrompt(String roleDesc, boolean includeHealing) {
        String prompt = roleDesc + """

                【审核原则】
                - 允许：情感倾诉、烦恼、心事、生活分享、轻微负面情绪、安慰鼓励。
                - 拒绝：色情、约炮、赌博、毒品、暴力、仇恨言论、广告推广、诈骗、政治敏感内容、联系方式、外部链接。
                - 不确定：介于两者之间或无法明确判断时，标记为 pending。

                【输出格式】
                只返回 JSON，不要包含其它文字：
                {
                  "decision": "approve" 或 "reject" 或 "pending",
                  "confidence": 0.0 到 1.0 的数字,
                  "reason": "一句话说明判断理由"
                }
                """;

        if (!includeHealing) {
            return prompt;
        }

        return prompt + """

                如果 decision 是 approve，请同时生成一段给发布者的“回想/治愈回应”：
                - healTag: 2 到 4 个中文字符，像一个轻柔标签，例如“会好的”“慢慢来”。
                - healingMessage: 20 到 60 个中文字符，温柔、克制、具体，回应内容里的情绪。
                - 不要说教，不要诊断，不要承诺现实结果，不要提到你是 AI。
                - 如果 decision 不是 approve，healTag 和 healingMessage 返回空字符串。

                JSON 额外包含：
                {
                  "healTag": "通过时填写，否则空字符串",
                  "healingMessage": "通过时填写，否则空字符串"
                }
                """;
    }

    @SuppressWarnings("unchecked")
    private ReviewAiResult parseApiResponse(Map responseBody, boolean includeHealing) {
        String aiContent = "";
        try {
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    aiContent = (String) message.get("content");
                }
            }
            return parseAiContent(aiContent, includeHealing);
        } catch (Exception e) {
            log.warn("解析 DeepSeek 响应失败: {}", e.getMessage());
            return new ReviewAiResult("pending", 0.0, "AI 审核无法确定，请管理员人工判断", "", "");
        }
    }

    private ReviewAiResult parseAiContent(String content, boolean includeHealing) throws Exception {
        String json = extractJson(content);
        JsonNode root = objectMapper.readTree(json);

        String decision = root.path("decision").asText("pending").trim();
        double confidence = root.path("confidence").asDouble(0.0);
        if (confidence > 1.0) {
            confidence = 1.0;
        }
        if (confidence < 0.0) {
            confidence = 0.0;
        }

        String reason = root.path("reason").asText("AI 审核无法确定，请管理员人工判断").trim();
        String healTag = includeHealing ? root.path("healTag").asText("").trim() : "";
        String healingMessage = includeHealing ? root.path("healingMessage").asText("").trim() : "";

        return new ReviewAiResult(decision, confidence, reason, healTag, healingMessage);
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        String cleaned = content.replaceAll("(?s)```(?:json)?", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private ReviewResult makeFinalDecision(Long entityId, ReviewAiResult aiResult, boolean includeHealing) {
        ReviewResult result;
        String decision = aiResult.decision();
        double confidence = aiResult.confidence();
        String reason = aiResult.reason();

        switch (decision) {
            case "reject":
                if (confidence >= REJECT_THRESHOLD) {
                    result = new ReviewResult(false, confidence, reason, null, null);
                } else {
                    result = pending("AI 认为可能违规，转人工复核: " + reason, confidence);
                }
                break;
            case "approve":
                if (confidence >= CONFIDENCE_THRESHOLD) {
                    result = approved(confidence, reason, includeHealing, aiResult);
                } else {
                    result = pending("AI 认为内容可能合规但不太确定，转人工审核: " + reason, confidence);
                }
                break;
            case "pending":
            default:
                result = pending("AI 无法确定，转人工审核: " + reason, confidence);
                break;
        }

        saveLog(entityId, result);
        log.debug("DeepSeek 审核 entityId={} -> approved={}, confidence={}, reason={}",
                entityId, result.approved(), String.format("%.2f", result.confidence()), result.reason());
        return result;
    }

    private ReviewResult approved(double confidence, String reason, boolean includeHealing, ReviewAiResult aiResult) {
        if (!includeHealing) {
            return new ReviewResult(true, confidence, reason, null, null);
        }

        HealingOption fallback = HEALING_OPTIONS.get(ThreadLocalRandom.current().nextInt(HEALING_OPTIONS.size()));
        String healTag = hasText(aiResult.healTag()) ? truncate(aiResult.healTag(), 12) : fallback.tag();
        String healingMessage = hasText(aiResult.healingMessage())
                ? truncate(aiResult.healingMessage(), 120)
                : fallback.message();

        return new ReviewResult(true, confidence, reason, healTag, healingMessage);
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ReviewAiResult(
            String decision,
            double confidence,
            String reason,
            String healTag,
            String healingMessage
    ) {}

    private record HealingOption(String tag, String message) {}
}
