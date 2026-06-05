package com.starweave.entity;

import java.time.LocalDateTime;

public class CatchHistory {
    private Long id;
    private Long userId;
    private Long meteorId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMeteorId() { return meteorId; }
    public void setMeteorId(Long meteorId) { this.meteorId = meteorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
