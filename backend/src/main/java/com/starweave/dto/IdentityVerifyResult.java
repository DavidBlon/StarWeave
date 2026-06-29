package com.starweave.dto;

public class IdentityVerifyResult {

    private boolean matched;
    private String bizCode;
    private String requestId;

    public IdentityVerifyResult() {
    }

    public IdentityVerifyResult(boolean matched, String bizCode, String requestId) {
        this.matched = matched;
        this.bizCode = bizCode;
        this.requestId = requestId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
