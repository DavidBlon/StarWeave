package com.starweave.dto;

public class CaptchaResponse {
    private String captchaId;
    private String image;
    private long expiresInSeconds;

    public CaptchaResponse() {}

    public CaptchaResponse(String captchaId, String image, long expiresInSeconds) {
        this.captchaId = captchaId;
        this.image = image;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
