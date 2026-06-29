package com.starweave.dto;

import jakarta.validation.constraints.NotBlank;

public class IdentityVerifyRequest {

    @NotBlank(message = "userName is required")
    private String userName;

    @NotBlank(message = "identifyNum is required")
    private String identifyNum;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdentifyNum() {
        return identifyNum;
    }

    public void setIdentifyNum(String identifyNum) {
        this.identifyNum = identifyNum;
    }
}
