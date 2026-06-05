package com.starweave.entity;

import java.time.LocalDateTime;

public class Wish {
    private Long id;
    private Long meteorId;
    private Long userId;
    private String content;
    private String status;
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private String replierNickname;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMeteorId() { return meteorId; }
    public void setMeteorId(Long meteorId) { this.meteorId = meteorId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) { this.reviewReason = reviewReason; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReplierNickname() { return replierNickname; }
    public void setReplierNickname(String replierNickname) { this.replierNickname = replierNickname; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
