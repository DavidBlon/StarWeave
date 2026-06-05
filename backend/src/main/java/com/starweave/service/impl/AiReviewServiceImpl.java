package com.starweave.service.impl;

import com.starweave.entity.AiReviewLog;
import com.starweave.entity.Message;
import com.starweave.mapper.AiReviewLogMapper;
import com.starweave.service.AiReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 关键词 + 规则过滤的审核实现
 *
 * 阶段一使用关键词匹配，未来可替换为：
 * 1. 接入 LLM API（如 Claude API）进行语义审核
 * 2. 接入第三方内容安全服务
 */
@Service
public class AiReviewServiceImpl implements AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewServiceImpl.class);

    private final AiReviewLogMapper reviewLogMapper;
    private final Random random = new Random();

    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile("(?i)(色情|裸[聊体照]|援交|约[炮火]|成人片)"),
            Pattern.compile("(?i)(赌博|赌场|博彩|六合彩|彩票)"),
            Pattern.compile("(?i)(毒品|吸毒|冰毒|海洛因|大麻)"),
            Pattern.compile("(?i)(枪支|弹药|炸药|刀|凶器)"),
            Pattern.compile("(?i)(诈骗|传销|洗钱|非法集资)"),
            Pattern.compile("(?i)(政治|习近平|共产党|法轮功|台独|藏独)"),
            Pattern.compile("(?i)(QQ群|微信号|手机号|1[3-9]\\d{9})"),
            Pattern.compile("(?i)(http[s]?://|www\\.)[\\w./]+")
    );

    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MIN_CONTENT_LENGTH = 1;

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

    public AiReviewServiceImpl(AiReviewLogMapper reviewLogMapper) {
        this.reviewLogMapper = reviewLogMapper;
    }

    @Override
    @Transactional
    public ReviewResult review(Message message) {
        String content = message.getContent();

        // 1. 长度检查
        if (content == null || content.length() < MIN_CONTENT_LENGTH) {
            return reject("内容为空", 0.99);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            return reject("内容超出长度限制", 0.95);
        }

        // 2. 敏感词检查
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return reject("内容包含不合适的关键词", 0.90);
            }
        }

        // 3. 重复内容检查
        if (content.replaceAll("(.)\\1{49,}", "$1").length() < content.length() - 50) {
            return reject("内容包含大量重复字符", 0.85);
        }

        // 审核通过 - 随机分配治愈标签
        HealingOption healing = HEALING_OPTIONS.get(random.nextInt(HEALING_OPTIONS.size()));
        ReviewResult result = new ReviewResult(true, 0.95, "自动审核通过", healing.tag, healing.message);
        saveLog(message.getId(), result);
        log.debug("消息 {} 审核通过，标签: {}", message.getId(), healing.tag);
        return result;
    }

    private ReviewResult reject(String reason, double confidence) {
        ReviewResult result = new ReviewResult(false, confidence, reason, null, null);
        log.debug("审核拒绝: {} (confidence={})", reason, confidence);
        return result;
    }

    private void saveLog(Long messageId, ReviewResult result) {
        AiReviewLog log = new AiReviewLog();
        log.setMessageId(messageId);
        log.setResult(result.approved() ? "approved" : "rejected");
        log.setConfidence(BigDecimal.valueOf(result.confidence()));
        log.setReason(result.reason());
        reviewLogMapper.insert(log);
    }

    private record HealingOption(String tag, String message) {}
}
