package com.starweave.dto;

public class UserStats {
    private long publishedCount;
    private long caughtCount;
    private long wishCount;

    public UserStats() {}

    public UserStats(long publishedCount, long caughtCount, long wishCount) {
        this.publishedCount = publishedCount;
        this.caughtCount = caughtCount;
        this.wishCount = wishCount;
    }

    public long getPublishedCount() { return publishedCount; }
    public void setPublishedCount(long publishedCount) { this.publishedCount = publishedCount; }

    public long getCaughtCount() { return caughtCount; }
    public void setCaughtCount(long caughtCount) { this.caughtCount = caughtCount; }

    public long getWishCount() { return wishCount; }
    public void setWishCount(long wishCount) { this.wishCount = wishCount; }
}
