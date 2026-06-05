package com.starweave.entity;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long userId;
    private String content;
    private String color;
    private String status;
    private String reviewReason;
    private String healTag;
    private String healingMessage;
    private Integer wishCount;
    private Boolean isCaught;
    private Long caughtBy;
    private LocalDateTime caughtAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) { this.reviewReason = reviewReason; }

    public String getHealTag() { return healTag; }
    public void setHealTag(String healTag) { this.healTag = healTag; }

    public String getHealingMessage() { return healingMessage; }
    public void setHealingMessage(String healingMessage) { this.healingMessage = healingMessage; }

    public Integer getWishCount() { return wishCount; }
    public void setWishCount(Integer wishCount) { this.wishCount = wishCount; }

    public Boolean getIsCaught() { return isCaught; }
    public void setIsCaught(Boolean isCaught) { this.isCaught = isCaught; }

    public Long getCaughtBy() { return caughtBy; }
    public void setCaughtBy(Long caughtBy) { this.caughtBy = caughtBy; }

    public LocalDateTime getCaughtAt() { return caughtAt; }
    public void setCaughtAt(LocalDateTime caughtAt) { this.caughtAt = caughtAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
