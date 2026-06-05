package com.starweave.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfig {
    // 头像通过 UserController.getRawAvatar() 端点提供，
    // 避免 Windows 下 "file:" URI 格式问题
}
