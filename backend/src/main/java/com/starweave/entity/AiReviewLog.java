package com.starweave.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AiReviewLog {
    private Long id;
    private Long messageId;
    private String result;
    private BigDecimal confidence;
    private String reason;
    private LocalDateTime reviewedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
