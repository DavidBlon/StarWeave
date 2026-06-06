package com.starweave.entity;

import java.time.LocalDateTime;

public class AfdianAccountLink {
    private Long id;
    private Long userId;
    private String afdianUserId;
    private String afdianUserPrivateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAfdianUserId() { return afdianUserId; }
    public void setAfdianUserId(String afdianUserId) { this.afdianUserId = afdianUserId; }

    public String getAfdianUserPrivateId() { return afdianUserPrivateId; }
    public void setAfdianUserPrivateId(String afdianUserPrivateId) { this.afdianUserPrivateId = afdianUserPrivateId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
