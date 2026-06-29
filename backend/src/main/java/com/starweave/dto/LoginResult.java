package com.starweave.dto;

import com.starweave.entity.User;

/**
 * 登录/注册成功后返回的用户信息 + JWT token
 */
public class LoginResult {
    private User user;
    private String token;

    public LoginResult() {}

    public LoginResult(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
