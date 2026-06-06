package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.dto.CaptchaResponse;
import com.starweave.service.CaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping
    public ApiResponse<CaptchaResponse> generate() {
        return ApiResponse.success(captchaService.generate());
    }
}
